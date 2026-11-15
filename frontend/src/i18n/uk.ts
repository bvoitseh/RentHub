/** All Ukrainian UI texts live here (split per feature into src/i18n/uk/<feature>.ts when this file nears 150 lines). */
export const uk = {
  app: {
    name: 'RentHub',
    tagline: 'Оренда обладнання між людьми та компаніями.',
  },
  nav: {
    main: 'Головна навігація',
    home: 'Головна',
  },
  home: {
    title: 'Орендуйте обладнання поруч із вами',
    subtitle: 'Будівельні інструменти, фото- та відеотехніка, спеціалізоване обладнання — від власників у вашому місті.',
  },
  notFound: {
    title: 'Сторінку не знайдено',
    description: 'Можливо, посилання застаріле або містить помилку.',
    backHome: 'На головну',
  },
  status: {
    checking: 'Перевірка з’єднання…',
    serverUp: 'Сервер працює',
    serverDown: 'Сервер недоступний',
  },
  common: {
    loading: 'Завантаження…',
    retry: 'Спробувати ще раз',
    errorTitle: 'Сталася помилка',
    errorGeneric: 'Щось пішло не так. Спробуйте пізніше.',
    errorNetwork: 'Немає з’єднання із сервером. Перевірте мережу та спробуйте ще раз.',
  },
} as const
