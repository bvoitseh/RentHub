/** Texts for the home-page recommendations (FR-16, FR-17). */
export const recommendations = {
  title: 'Рекомендації поруч',
  area: (place: string) => `Район пошуку: ${place}`,
  yourLocation: 'ваше розташування',
  useLocation: 'Використати моє розташування',
  useCity: 'Показати для мого міста',
  locating: 'Визначаємо розташування…',
  denied: 'Не вдалося отримати геолокацію браузера',
  consentHint: 'Дозвольте обробку геолокації в профілі, щоб бачити обладнання поруч із вами.',
  privacy: 'Координати використовуються лише для цього запиту й не зберігаються.',
  season: 'Сезон',
  fromCentre: (km: number) => `${km} км від центру`,
  empty: 'Поблизу поки немає обладнання',
  emptyHint: 'Подивіться весь каталог.',
  toCatalog: 'Перейти до каталогу',
} as const
