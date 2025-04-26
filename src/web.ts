import { WebPlugin } from '@capacitor/core';

import type { SecureStoragePlugin } from './definitions';

export class SecureStorageWeb extends WebPlugin implements SecureStoragePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
