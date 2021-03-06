package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.IdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.artifact.Saml1ArtifactResolutionProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.ecp.ECPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLORedirectProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOProfileCallbackHandlerController;
import org.apereo.cas.ticket.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPEndpointsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPEndpointsConfiguration {
    @Autowired
    @Qualifier("casClientTicketValidator")
    private AbstractUrlBasedTicketValidator casClientTicketValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("samlObjectSigner")
    private BaseSamlObjectSigner samlObjectSigner;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    private MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier("samlProfileSamlSoap11ResponseBuilder")
    private SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11ResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSoap11FaultResponseBuilder")
    private SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11FaultResponseBuilder;


    @Autowired
    @Qualifier("samlProfileSamlArtifactResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlArtifactResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlArtifactFaultResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlArtifactFaultResponseBuilder;
            
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlIdPObjectSignatureValidator() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlIdPObjectSignatureValidator(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms(),
                casSamlIdPMetadataResolver
        );
    }

    @ConditionalOnMissingBean(name = "samlObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlObjectSignatureValidator() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectSignatureValidator(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms()
        );
    }

    @Bean
    @RefreshScope
    public SSOPostProfileHandlerController ssoPostProfileHandlerController() {
        return new SSOPostProfileHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlResponseBuilder,
                casProperties,
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SLORedirectProfileHandlerController sloRedirectProfileHandlerController() {
        return new SLORedirectProfileHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlResponseBuilder,
                casProperties,
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SLOPostProfileHandlerController sloPostProfileHandlerController() {
        return new SLOPostProfileHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlResponseBuilder,
                casProperties,
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public IdPInitiatedProfileHandlerController idPInitiatedSamlProfileHandlerController() {
        return new IdPInitiatedProfileHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlResponseBuilder,
                casProperties,
                samlIdPObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SSOProfileCallbackHandlerController ssoPostProfileCallbackHandlerController() {
        return new SSOProfileCallbackHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlResponseBuilder,
                casProperties,
                samlObjectSignatureValidator(),
                this.casClientTicketValidator);
    }

    @Bean
    @RefreshScope
    public ECPProfileHandlerController ecpProfileHandlerController() {
        return new ECPProfileHandlerController(samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlSoap11ResponseBuilder,
                samlProfileSamlSoap11FaultResponseBuilder,
                casProperties,
                samlObjectSignatureValidator());
    }

    @Autowired
    @Bean
    @RefreshScope
    public Saml1ArtifactResolutionProfileHandlerController saml1ArtifactResolutionController(
            @Qualifier("samlArtifactTicketFactory") final SamlArtifactTicketFactory samlArtifactTicketFactory) {
        return new Saml1ArtifactResolutionProfileHandlerController(
                samlObjectSigner,
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                openSamlConfigBean,
                samlProfileSamlArtifactResponseBuilder,
                casProperties,
                samlObjectSignatureValidator(),
                ticketRegistry, 
                samlArtifactTicketFactory, 
                samlProfileSamlArtifactFaultResponseBuilder);
    }
}
