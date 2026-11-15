/** Client-side validation messages (Zod); the backend repeats the same rules with its own Ukrainian texts. */
export const validation = {
  required: 'Поле обов’язкове',
  email: 'Некоректна адреса електронної пошти',
  phone: 'Номер телефону має бути у форматі +380XXXXXXXXX',
  passwordMin: 'Пароль має містити щонайменше 8 символів',
  passwordMax: 'Пароль має містити не більше 72 символів',
  passwordsMismatch: 'Паролі не збігаються',
  nameMax: 'Ім’я має містити не більше 100 символів',
  cityMax: 'Назва міста має містити не більше 100 символів',
  ownerTypeRequired: 'Оберіть тип власника',
  companyNameRequired: 'Вкажіть назву компанії',
  edrpou: 'Код ЄДРПОУ має складатися з 8 цифр',
} as const
