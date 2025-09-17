<script lang="ts">
  import { Editor } from "@tiptap/core"
  import StarterKit from "@tiptap/starter-kit"
  import { Placeholder } from "@tiptap/extensions"
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { onDestroy } from "svelte"
  import type { EditorContent } from "$lib/utils/EditorUtils"

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

  let editor: Editor | undefined = $state()
  let editorState = $state<EditorState>(EMPTY_EDITOR_STATE)
  let linkModalRef = $state<HTMLDialogElement>()
  let currentLinkState = $state<string>()

  export const getValue = (): EditorContent => {
    return editor?.getJSON() ?? { type: "doc", content: [] }
  }

  const createEditor = (element: HTMLDivElement) => {
    editor = new Editor({
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
        Placeholder.configure({
          placeholder: "Write something...",
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

{#if editor}
  <dialog bind:this={linkModalRef} class="modal">
    <div class="modal-box">
      <form method="dialog">
        <button class="btn absolute top-2 right-2 btn-circle btn-ghost btn-sm">✕</button>
      </form>
      <form onsubmit={setLink}>
        <fieldset class="fieldset">
          <label class="label" for="editor-link-url">URL</label>
          <input
            class="validator input w-full"
            id="editor-link-url"
            type="url"
            required
            placeholder="https://"
            title="Must be valid URL"
            bind:value={currentLinkState}
          />
          <div class="validator-hint">Enter a valid URL</div>
        </fieldset>
        <button type="submit" class="btn btn-sm btn-primary"> Save</button>
        <button type="button" class="btn btn-sm" onclick={deleteLink}> Delete Link</button>
      </form>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button>close</button>
    </form>
  </dialog>
  <div class="mb-1 flex w-full flex-wrap gap-1">
    <div class="join">
      <button
        type="button"
        onclick={() => editor?.chain().focus().setParagraph().run()}
        class={["btn join-item btn-xs btn-primary", !editorState.paragraph && "btn-soft"]}
      >
        Normal text
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
        class={["btn join-item btn-xs btn-primary", !editorState.heading[1] && "btn-soft"]}
      >
        H1
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
        class={["btn join-item btn-xs btn-primary", !editorState.heading[2] && "btn-soft"]}
      >
        H2
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleHeading({ level: 3 }).run()}
        class={["btn join-item btn-xs btn-primary", !editorState.heading[3] && "btn-soft"]}
      >
        H3
      </button>
    </div>
    <div class="join">
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleBulletList().run()}
        class={["btn join-item btn-xs btn-primary", !editorState.bulletList && "btn-soft"]}
      >
        Bullet list
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleOrderedList().run()}
        class={["btn join-item btn-xs btn-primary", !editorState.orderedList && "btn-soft"]}
      >
        Ordered list
      </button>
    </div>
    <button
      type="button"
      onclick={() => editor?.chain().focus().toggleCodeBlock().run()}
      class={["btn btn-xs btn-primary", !editorState.codeBlock && "btn-soft"]}
    >
      Code block
    </button>
    <button
      type="button"
      onclick={() => editor?.chain().focus().toggleBlockquote().run()}
      class={["btn btn-xs btn-primary", !editorState.blockquote && "btn-soft"]}
    >
      Quote
    </button>
    <div class="divider m-0 divider-horizontal"></div>
    <div class="join">
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleBold().run()}
        disabled={!editorState.bold.can}
        class={["btn join-item btn-xs btn-primary", !editorState.bold.active && "btn-soft"]}
      >
        Bold
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleItalic().run()}
        disabled={!editorState.italic.can}
        class={["btn join-item btn-xs btn-primary", !editorState.italic.active && "btn-soft"]}
      >
        Italic
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleUnderline().run()}
        disabled={!editorState.underline.can}
        class={["btn join-item btn-xs btn-primary", !editorState.underline.active && "btn-soft"]}
      >
        Underline
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().toggleStrike().run()}
        disabled={!editorState.strike.can}
        class={["btn join-item btn-xs btn-primary", !editorState.strike.active && "btn-soft"]}
      >
        Strike
      </button>
    </div>
    <button
      type="button"
      onclick={showLinkModal}
      class={["btn btn-xs btn-primary", !editorState.link && "btn-soft"]}
    >
      Link
    </button>
    <button
      type="button"
      onclick={() => editor?.chain().focus().toggleCode().run()}
      disabled={!editorState.code.can}
      class={["btn btn-xs btn-primary", !editorState.code.active && "btn-soft"]}
    >
      Code
    </button>
    <div class="divider m-0 divider-horizontal"></div>
    <button
      type="button"
      onclick={() => editor?.chain().focus().setHorizontalRule().run()}
      class="btn btn-soft btn-xs btn-primary"
    >
      Horizontal rule
    </button>
    <div class="divider m-0 divider-horizontal"></div>
    <div class="join">
      <button
        type="button"
        onclick={() => editor?.chain().focus().undo().run()}
        disabled={!editorState.undo}
        class="btn join-item btn-soft btn-xs btn-primary"
      >
        Undo
      </button>
      <button
        type="button"
        onclick={() => editor?.chain().focus().redo().run()}
        disabled={!editorState.redo}
        class="btn join-item btn-soft btn-xs btn-primary"
      >
        Redo
      </button>
    </div>
  </div>
{/if}
<div class="textarea w-full text-base" use:createEditor></div>
