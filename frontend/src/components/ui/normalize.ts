/** Search key: lower case, apostrophes removed (П’ятниця = пятниця), the same rule as the settlement search. */
export const searchKey = (text: string) =>
  text
    .replace(/[’'ʼ`]/g, '')
    .toLocaleLowerCase('uk')
    .trim()
