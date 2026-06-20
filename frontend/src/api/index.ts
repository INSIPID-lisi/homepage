import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface UserProfile {
  id: number
  name: string
  avatar: string | null
  bio: string | null
}

export interface SocialLink {
  id: number
  platform: string
  url: string
  icon: string | null
}

export interface Post {
  id: number
  title: string
  content: string | null
  type: string
  pinned: boolean
  createdAt: string
  updatedAt: string
}

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('Request failed:', error.message)
    return Promise.reject(error)
  }
)

export function getProfile() {
  return request.get<Result<UserProfile>>('/profile')
}

export function getSocialLinks() {
  return request.get<Result<SocialLink[]>>('/social-links')
}

export function getPosts(type?: string, search?: string) {
  const params: Record<string, string> = {}
  if (type) params.type = type
  if (search) params.search = search
  return request.get<Result<Post[]>>('/posts', { params })
}

export function getPost(id: number) {
  return request.get<Result<Post>>(`/posts/${id}`)
}

export function createPost(data: { title: string; content?: string; type: string; pinned?: boolean }) {
  return request.post<Result<null>>('/posts', data)
}

export function updatePost(id: number, data: { title: string; content?: string; type: string; pinned?: boolean }) {
  return request.put<Result<null>>(`/posts/${id}`, data)
}

export function deletePost(id: number) {
  return request.delete<Result<null>>(`/posts/${id}`)
}

export interface Anime {
  id: number
  name: string
  coverUrl: string | null
  description: string | null
  latestReview: string | null
  latestReviewTime: string | null
  createdAt: string
  updatedAt: string
}

export interface AnimeReview {
  id: number
  animeId: number
  content: string
  createdTime: string
}

export interface AnimeDetail {
  id: number
  name: string
  coverUrl: string | null
  description: string | null
  createdAt: string
  updatedAt: string
  reviews: AnimeReview[]
}

export function getAnimeList() {
  return request.get<Result<Anime[]>>('/anime')
}

export function getAnimeDetail(id: number) {
  return request.get<Result<AnimeDetail>>(`/anime/${id}`)
}

export function createAnime(data: { name: string; coverUrl?: string; description?: string }) {
  return request.post<Result<null>>('/anime', data)
}

export function updateAnime(id: number, data: { name?: string; coverUrl?: string; description?: string }) {
  return request.put<Result<null>>(`/anime/${id}`, data)
}

export function deleteAnime(id: number) {
  return request.delete<Result<null>>(`/anime/${id}`)
}

export function createAnimeReview(animeId: number, data: { content: string }) {
  return request.post<Result<null>>(`/anime/${animeId}/reviews`, data)
}

export function updateAnimeReview(animeId: number, reviewId: number, data: { content: string }) {
  return request.put<Result<null>>(`/anime/${animeId}/reviews/${reviewId}`, data)
}

export function deleteAnimeReview(animeId: number, reviewId: number) {
  return request.delete<Result<null>>(`/anime/${animeId}/reviews/${reviewId}`)
}

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Result<string>>('/upload', formData)
}

export function checkAdmin() {
  return request.get<Result<boolean>>('/admin/check')
}

export default request
