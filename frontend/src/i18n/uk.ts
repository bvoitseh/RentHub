import { address } from '@/i18n/uk/address'
import { admin } from '@/i18n/uk/admin'
import { adminCatalog } from '@/i18n/uk/adminCatalog'
import { auth } from '@/i18n/uk/auth'
import { booking } from '@/i18n/uk/booking'
import { catalog } from '@/i18n/uk/catalog'
import { complaints } from '@/i18n/uk/complaints'
import { listings } from '@/i18n/uk/listings'
import { privacy } from '@/i18n/uk/privacy'
import { profile } from '@/i18n/uk/profile'
import { recommendations } from '@/i18n/uk/recommendations'
import { reviews } from '@/i18n/uk/reviews'
import { validation } from '@/i18n/uk/validation'

/** All Ukrainian UI texts. Feature texts live in src/i18n/uk/<feature>.ts and are composed here. */
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
    or: 'або',
  },
  address,
  admin,
  adminCatalog,
  auth,
  booking,
  catalog,
  complaints,
  listings,
  privacy,
  profile,
  recommendations,
  reviews,
  validation,
} as const
