<script lang="ts">
  import { Editor } from "@tiptap/core"
  import StarterKit from "@tiptap/starter-kit"
  import { Placeholder } from "@tiptap/extensions"
  import Underline from "@tiptap/extension-underline"
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { onDestroy } from "svelte"
  import type { EditorContent } from "$lib/utils/EditorUtils"
  import { m } from "$lib/paraglide/messages.js"

  interface EditorProps {
    id?: string
    labelledBy?: string
    dataTestId?: string
  }

  interface EditorStateItem {
    can: boolean
    active: boolean
  }

  interface EditorState {
    bold: EditorStateItem
    italic: EditorStateItem
    strike: EditorStateItem
    underline: EditorStateItem
    code: EditorStateItem
    link: boolean
    paragraph: boolean
    heading: {
      1: boolean
      2: boolean
      3: boolean
    }
    bulletList: boolean
    orderedList: boolean
    codeBlock: boolean
    blockquote: boolean
    undo: boolean
    redo: boolean
  }

  const EMPTY_EDITOR_STATE_ITEM: EditorStateItem = {
    can: false,
    active: false,
  }

  const EMPTY_EDITOR_STATE: EditorState = {
    bold: EMPTY_EDITOR_STATE_ITEM,
    italic: EMPTY_EDITOR_STATE_ITEM,
    strike: EMPTY_EDITOR_STATE_ITEM,
    underline: EMPTY_EDITOR_STATE_ITEM,
    code: EMPTY_EDITOR_STATE_ITEM,
    link: false,
    paragraph: false,
    heading: {
      1: false,
      2: false,
      3: false,
    },
    bulletList: false,
    orderedList: false,
    codeBlock: false,
    blockquote: false,
    undo: false,
    redo: false,
  }

  let { id, labelledBy, dataTestId }: EditorProps = $props()
  let editor: Editor | undefined = $state()
  let editorState = $state<EditorState>(EMPTY_EDITOR_STATE)
  let linkModalRef = $state<HTMLDialogElement>()
  let currentLinkState = $state<string | undefined>()

  const withSoft = (base: string, isActive: boolean) => (isActive ? base : `${base} btn-soft`)

  export const getValue = (): EditorContent => {
    return editor?.getJSON() ?? { type: "doc", content: [] }
  }
  export const getValueHtml = (): string => {
    return editor?.getHTML() ?? ""
  }

  export const setHtmlValue = (value: string) => {
    editor?.commands.setContent(value, { emitUpdate: false })
  }

  const createEditor = (element: HTMLDivElement) => {
    editor = new Editor({
      editorProps: {
        attributes: {
          ...(id && { id }),
          ...(labelledBy && { "aria-labelledby": labelledBy }),
        },
      },
      element: element,
      extensions: [
        StarterKit.configure({
          heading: {
            levels: [1, 2, 3],
          },
          link: {
            openOnClick: false,
            autolink: true,
            enableClickSelection: true,
          },
        }),
        Underline,
        Placeholder.configure({
          placeholder: m.drafting_placeholder(),
        }),
      ],
      onTransaction: (e) => {
        editor = e.editor
        editorState = {
          bold: {
            active: editor.isActive("bold"),
            can: editor.can().chain().focus().toggleBold().run(),
          },
          italic: {
            active: editor.isActive("italic"),
            can: editor.can().chain().focus().toggleItalic().run(),
          },
          strike: {
            active: editor.isActive("strike"),
            can: editor.can().chain().focus().toggleStrike().run(),
          },
          underline: {
            active: editor.isActive("underline"),
            can: editor.can().chain().focus().toggleUnderline().run(),
          },
          code: {
            active: editor.isActive("code"),
            can: editor.can().chain().focus().toggleCode().run(),
          },
          link: editor.isActive("link"),
          paragraph: editor.isActive("paragraph"),
          heading: {
            1: editor.isActive("heading", { level: 1 }),
            2: editor.isActive("heading", { level: 2 }),
            3: editor.isActive("heading", { level: 3 }),
          },
          bulletList: editor.isActive("bulletList"),
          orderedList: editor.isActive("orderedList"),
          codeBlock: editor.isActive("codeBlock"),
          blockquote: editor.isActive("blockquote"),
          undo: editor.can().chain().focus().undo().run(),
          redo: editor.can().chain().focus().redo().run(),
        }
      },
    })
  }

  onDestroy(() => {
    editor?.destroy()
  })

  const showLinkModal = () => {
    currentLinkState = editor?.getAttributes("link").href
    linkModalRef?.showModal()
  }

  const setLink = (event: SubmitEvent) => {
    CsrFormHandler.onsubmit(event)
    try {
      if (currentLinkState) {
        editor?.chain().focus().extendMarkRange("link").setLink({ href: currentLinkState }).run()
      } else {
        editor?.chain().focus().extendMarkRange("link").unsetLink().run()
      }
      linkModalRef?.close()
    } catch {
      console.error("Could not set link")
    }
  }

  const deleteLink = () => {
    currentLinkState = undefined
    editor?.chain().focus().extendMarkRange("link").unsetLink().run()
    linkModalRef?.close()
  }
</script>

<div class="textarea w-full">
  {#if editor}
    <dialog bind:this={linkModalRef} class="modal">
      <div class="modal-box pt-8">
        <form method="dialog">
          <button class="btn absolute top-2 right-2 btn-circle btn-ghost btn-sm">✕</button>
        </form>
        <form onsubmit={setLink}>
          <fieldset class="fieldset">
            <label class="label" for="editor-link-url">{m.editor_link_url_label()}</label>
            <input
              dir="ltr"
              class="validator input w-full"
              id="editor-link-url"
              type="url"
              required
              placeholder={m.editor_link_placeholder()}
              title={m.editor_link_title()}
              bind:value={currentLinkState}
            />
            <div class="validator-hint">{m.editor_link_url_hint()}</div>
          </fieldset>
          <button type="submit" class="btn btn-sm btn-primary">{m.form_save()}</button>
          <button type="button" class="btn btn-sm" onclick={deleteLink}
            >{m.editor_toolbar_link_delete()}</button
          >
        </form>
      </div>
      <form method="dialog" class="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
    <div class="mb-3 flex w-full flex-wrap gap-1">
      <div class="join">
        <button
          type="button"
          onclick={() => editor?.chain().focus().setParagraph().run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.paragraph)}
        >
          {m.editor_toolbar_normal_text()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.heading[1])}
        >
          {m.editor_toolbar_h1()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.heading[2])}
        >
          {m.editor_toolbar_h2()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleHeading({ level: 3 }).run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.heading[3])}
        >
          {m.editor_toolbar_h3()}
        </button>
      </div>
      <div class="join">
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleBulletList().run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.bulletList)}
        >
          {m.editor_toolbar_bullet_list()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleOrderedList().run()}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.orderedList)}
        >
          {m.editor_toolbar_ordered_list()}
        </button>
      </div>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleCodeBlock().run()}
        class={withSoft("btn btn-xs btn-primary", editorState.codeBlock)}
      >
        {m.editor_toolbar_code_block()}
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleBlockquote().run()}
        class={withSoft("btn btn-xs btn-primary", editorState.blockquote)}
      >
        {m.editor_toolbar_quote()}
      </button>
      <div class="divider m-0 divider-horizontal"></div>
      <div class="join">
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleBold().run()}
          disabled={!editorState.bold.can}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.bold.active)}
        >
          {m.editor_toolbar_bold()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleItalic().run()}
          disabled={!editorState.italic.can}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.italic.active)}
        >
          {m.editor_toolbar_italic()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleUnderline().run()}
          disabled={!editorState.underline.can}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.underline.active)}
        >
          {m.editor_toolbar_underline()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().toggleStrike().run()}
          disabled={!editorState.strike.can}
          class={withSoft("btn join-item btn-xs btn-primary", editorState.strike.active)}
        >
          {m.editor_toolbar_strike()}
        </button>
      </div>
      <button
        type="button"
        onclick={showLinkModal}
        class={withSoft("btn btn-xs btn-primary", editorState.link)}
      >
        {m.editor_toolbar_link()}
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleCode().run()}
        disabled={!editorState.code.can}
        class={withSoft("btn btn-xs btn-primary", editorState.code.active)}
      >
        {m.editor_toolbar_code_inline()}
      </button>
      <div class="divider m-0 divider-horizontal"></div>
      <button
        type="button"
        onclick={() => editor?.chain().focus().setHorizontalRule().run()}
        class="btn btn-soft btn-xs btn-primary"
      >
        {m.editor_toolbar_horizontal_rule()}
      </button>
      <div class="divider m-0 divider-horizontal"></div>
      <div class="join">
        <button
          type="button"
          onclick={() => editor?.chain().focus().undo().run()}
          disabled={!editorState.undo}
          class="btn join-item btn-soft btn-xs btn-primary"
        >
          {m.editor_toolbar_undo()}
        </button>
        <button
          type="button"
          onclick={() => editor?.chain().focus().redo().run()}
          disabled={!editorState.redo}
          class="btn join-item btn-soft btn-xs btn-primary"
        >
          {m.editor_toolbar_redo()}
        </button>
      </div>
    </div>
  {/if}
  <div class="text-base" use:createEditor data-testid={dataTestId}></div>
</div>
