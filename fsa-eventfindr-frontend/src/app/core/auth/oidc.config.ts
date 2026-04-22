import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

export const authCodeFlowConfig: AuthConfig = {
  issuer: environment.keyCloakUrl + '/realms/EventfindR',
  redirectUri: environment.appUrl + '/',
  clientId: 'eventfindr-client',
  responseType: 'code',
  scope: 'openid profile email offline_access',
  showDebugInformation: true,
  requireHttps: false,
  strictDiscoveryDocumentValidation: false,
};
