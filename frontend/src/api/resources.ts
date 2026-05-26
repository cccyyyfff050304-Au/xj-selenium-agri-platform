import http, { type PageResult } from './http'

export function pageResource<T>(url: string, params: Record<string, unknown>) {
  return http.get<PageResult<T>, PageResult<T>>(url, { params })
}

export function createResource<T>(url: string, data: Partial<T>) {
  return http.post<T, T>(url, data)
}

export function updateResource<T>(url: string, id: number, data: Partial<T>) {
  return http.put<T, T>(`${url}/${id}`, data)
}

export function deleteResource(url: string, id: number) {
  return http.delete<boolean, boolean>(`${url}/${id}`)
}
