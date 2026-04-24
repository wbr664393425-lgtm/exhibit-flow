import CryptoJS from 'crypto-js';

const ENC_KEY = 'thanks,pig4cloud';

export function encryptPassword(password: string): string {
  const key = CryptoJS.enc.Utf8.parse(ENC_KEY);
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding,
  });
  return encrypted.toString();
}
