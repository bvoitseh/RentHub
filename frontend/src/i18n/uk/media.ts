/** Listing photos and video (FR-4a, D-52). */
export const media = {
  photosHint:
    'JPEG, PNG або WebP до 5 МБ, від 1 до 10 фото. Можна обрати кілька файлів одразу. Перше фото — обкладинка.',
  dropPhotos: 'Перетягніть фото сюди або',
  choosePhotos: 'оберіть файли',
  uploadingOf: (done: number, total: number) => `Завантаження ${done} з ${total}…`,
  notUploaded: 'Не завантажено:',
  tooManyPhotos: (max: number) => `перевищено ліміт у ${max} фото`,
  photoType: 'потрібен JPEG, PNG або WebP',
  photoSize: 'файл більший за 5 МБ',
  video: 'Відео',
  videoHint: 'Одне відео MP4 або WebM до 50 МБ. Нове відео замінить попереднє.',
  dropVideo: 'Перетягніть відео сюди або',
  chooseVideo: 'оберіть файл',
  videoType: 'Підтримуються лише відео MP4 або WebM',
  videoSize: 'Відео має бути не більшим за 50 МБ',
  videoUploading: (percent: number) => `Завантаження відео… ${percent}%`,
  removeVideo: 'Видалити відео',
  playVideo: 'Відео',
} as const
