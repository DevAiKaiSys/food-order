export const environment = {
    production: true,
    apiUrl: (window as any).__env?.apiUrl || 'http://my-prod-url'
};