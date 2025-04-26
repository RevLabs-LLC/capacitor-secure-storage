export interface SecureStoragePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
