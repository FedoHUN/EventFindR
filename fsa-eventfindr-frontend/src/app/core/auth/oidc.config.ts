import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

export const authCodeFlowConfig: AuthConfig = {
  issuer: `${environment.keyCloakUrl}/realms/${environment.keycloakRealm}`,
  redirectUri: environment.appUrl + '/',
  clientId: environment.keycloakClientId,
  responseType: 'code',
  scope: 'openid profile email offline_access',
  showDebugInformation: !environment.production,
  requireHttps: environment.production,
  strictDiscoveryDocumentValidation: environment.production,
};
