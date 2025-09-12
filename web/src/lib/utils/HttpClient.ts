export class HttpError extends Error {
  status: number
  body: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = "HttpError"
    this.status = status
    this.body = body
  }
}

export type CustomRequestInit = Omit<RequestInit, "body"> & {
  json?: unknown
}

export type AuthorizationHeaderProvider = () => Promise<string>

export class HttpClient {
  private readonly base: string
  private readonly defaultHeaders: HeadersInit
  private readonly defaultCredentials?: RequestCredentials
  private readonly authHeaderProvider?: AuthorizationHeaderProvider

  constructor(options?: {
    base?: string
    defaultHeaders?: HeadersInit
    defaultCredentials?: RequestCredentials
    authHeaderProvider?: AuthorizationHeaderProvider
  }) {
    this.base = options?.base ?? ""
    this.defaultHeaders = options?.defaultHeaders ?? {}
    this.defaultCredentials = options?.defaultCredentials
    this.authHeaderProvider = options?.authHeaderProvider
  }

  private async request<T>(url: string, init: CustomRequestInit = {}): Promise<T> {
    const { json, headers, ...rest } = init
    const hasBody = json !== undefined

    const res = await fetch(this.base + url, {
      ...rest,
      headers: {
        ...(hasBody ? { "Content-Type": "application/json" } : {}),
        ...this.defaultHeaders,
        ...(this.authHeaderProvider ? { Authorization: await this.authHeaderProvider() } : {}),
        ...headers,
      },
      body: hasBody ? JSON.stringify(json) : undefined,
      credentials: init.credentials ?? this.defaultCredentials,
    })

    const text = await res.text()
    const data = text ? JSON.parse(text) : null

    if (!res.ok) {
      throw new HttpError(
        res.status,
        (data && (data.message || data.error)) || res.statusText,
        data,
      )
    }

    return data as T
  }

  get<T>(url: string, init?: Omit<CustomRequestInit, "json" | "method">) {
    return this.request<T>(url, { ...init, method: "GET" })
  }

  post<T>(url: string, json?: unknown, init?: Omit<CustomRequestInit, "json" | "method">) {
    return this.request<T>(url, { ...init, method: "POST", json })
  }

  put<T>(url: string, json?: unknown, init?: Omit<CustomRequestInit, "json" | "method">) {
    return this.request<T>(url, { ...init, method: "PUT", json })
  }

  patch<T>(url: string, json?: unknown, init?: Omit<CustomRequestInit, "json" | "method">) {
    return this.request<T>(url, { ...init, method: "PATCH", json })
  }

  delete<T>(url: string, init?: Omit<CustomRequestInit, "json" | "method">) {
    return this.request<T>(url, { ...init, method: "DELETE" })
  }
}
