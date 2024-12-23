/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Server Configuration Management REST APIs success paths.
 */
public class ConfigSuccessTest extends ConfigTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConfigSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetAuthenticator() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_AUTHENTICATOR_API_BASE_PATH + PATH_SEPARATOR + SAMPLE_AUTHENTICATOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(SAMPLE_AUTHENTICATOR_ID))
                .body("name", equalTo("BasicAuthenticator"))
                .body("displayName", equalTo("Username & Password"))
                .body("isEnabled", equalTo(true))
                .body("properties", notNullValue())
                .body("definedBy", equalTo("SYSTEM"));
    }

    @Test(dependsOnMethods = {"testGetAuthenticator"})
    public void testGetAuthenticators() throws Exception {

        String baseIdentifier = "find{ it.id == '" + SAMPLE_AUTHENTICATOR_ID + "' }.";
        Response response = getResponseOfGet(CONFIGS_AUTHENTICATOR_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("BasicAuthenticator"))
                .body(baseIdentifier + "displayName", equalTo("Username & Password"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo("SYSTEM"));
    }

    @Test(dependsOnMethods = {"testGetAuthenticators"})
    public void testGetConfigs() throws Exception {

        Response response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        String adminUserName = MultitenantUtils.getTenantAwareUsername(context.getContextTenant().getTenantAdmin()
                .getUserName());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("idleSessionTimeoutPeriod", notNullValue())
                .body("rememberMePeriod", notNullValue())
                .body("homeRealmIdentifiers", notNullValue())
                .body("provisioning", notNullValue())
                .body("authenticators", notNullValue())
                .body("cors", notNullValue())
                .body("realmConfig", notNullValue())
                .body("realmConfig.adminUser", is(adminUserName))
                .body("realmConfig.adminRole", notNullValue())
                .body("realmConfig.everyoneRole", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetConfigs"})
    public void testGetHomeRealmIdentifiers() throws Exception {

        Response response = getResponseOfGet(HOME_REALM_IDENTIFIERS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("\"localhost\""));
    }

    @Test(dependsOnMethods = {"testGetConfigs"})
    public void testPatchConfigs() throws Exception {

        String body = readResource("patch-replace-configs.json");
        Response response = getResponseOfPatch(CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("idleSessionTimeoutPeriod", equalTo("20"));

        body = readResource("patch-add-configs.json");
        response = getResponseOfPatch(CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("homeRealmIdentifiers.contains(\"test-realm\")", equalTo(true));

        body = readResource("patch-remove-configs.json");
        response = getResponseOfPatch(CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("homeRealmIdentifiers.contains(\"test-realm\")", equalTo(false));
    }

    @Test(dependsOnMethods = {"testPatchConfigs"})
    public void testUpdateScimConfigs() throws Exception {

        String body = readResource("update-scim-configs.json");
        Response response = getResponseOfPut(CONFIGS_INBOUND_SCIM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("provisioning", notNullValue())
                .body("provisioning.inbound.scim.provisioningUserstore", equalTo("PRIMARY"))
                .body("provisioning.inbound.scim.enableProxyMode", equalTo(false));

        // Clearing added inbound scim config.
        String defaultBody = readResource("default-scim-configs.json");
        getResponseOfPut(CONFIGS_INBOUND_SCIM_API_BASE_PATH, defaultBody);
    }

    @Test
    public void testGetImpersonationConfigs() throws Exception {

        Response response = getResponseOfGet(IMPERSONATION_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enableEmailNotification", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetImpersonationConfigs"})
    public void testPatchImpersonationConfigs() throws Exception {


        String body = readResource("patch-modify-impersonation-configs.json");
        Response response = getResponseOfPatch(IMPERSONATION_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(IMPERSONATION_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enableEmailNotification", equalTo(false));

        // Clearing added inbound scim config.
        String defaultBody = readResource("default-impersonation-configs.json");
        getResponseOfPatch(IMPERSONATION_CONFIGS_API_BASE_PATH, defaultBody);
        response = getResponseOfGet(IMPERSONATION_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enableEmailNotification", equalTo(true));
    }

    @Test(priority = 1)
    public void testGetCORSConfigs() throws Exception {

        Response response = getResponseOfGet(CORS_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("allowGenericHttpRequests", notNullValue())
                .body("allowSubdomains", notNullValue())
                .body("supportedMethods", notNullValue())
                .body("supportedHeaders", notNullValue())
                .body("exposedHeaders", notNullValue())
                .body("supportsCredentials", notNullValue())
                .body("maxAge", notNullValue());
    }

    @Test(priority = 2)
    public void testPatchCORSConfigs() throws Exception {

        String body = readResource("patch-cors-configs.json");
        Response response = getResponseOfPatch(CORS_CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CORS_CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("allowGenericHttpRequests", equalTo(true))
                .body("allowSubdomains", equalTo(true))
                .body("supportedMethods", hasItem("POST"))
                .body("supportedHeaders", hasItem("Content-Type"))
                .body("exposedHeaders", hasItem("X-Custom-1"))
                .body("supportsCredentials", equalTo(false))
                .body("maxAge", equalTo(3600));
    }

    @Test
    public void testGetSAMLInboundAuthConfigs() throws XPathExpressionException {

        Response response = getResponseOfGet(SAML_INBOUND_AUTH_CONFIG_API_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("destinationURLs", notNullValue())
                .body("metadataValidityPeriod", equalTo(60))
                .body("enableMetadataSigning", equalTo(false))
                .body("metadataEndpoint",
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(context.getContextTenant().getDomain())
                                ? equalTo(SAML_METADATA_ENDPOINT_SUPER_TENANT)
                                : equalTo(SAML_METADATA_ENDPOINT_TENANT));

        String[] destinationUrls = response.jsonPath().getString("destinationURLs")
                .replace("[", "").replace("]", "").split(",");
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(context.getContextTenant().getDomain())) {
            Assert.assertArrayEquals(new String[]{SAML_SSO_URL_SUPER_TENANT}, destinationUrls);
        } else {
            Assert.assertArrayEquals(new String[]{SAML_SSO_URL_TENANT}, destinationUrls);
        }
    }

    @Test(dependsOnMethods = {"testGetSAMLInboundAuthConfigs"})
    public void testUpdateSAMLInboundAuthConfigs() throws IOException {

        String body = readResource("update-saml-inbound-auth-configs.json");
        Response response = getResponseOfPatch(SAML_INBOUND_AUTH_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(SAML_INBOUND_AUTH_CONFIG_API_PATH);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("destinationURLs", notNullValue())
                .body("metadataValidityPeriod", equalTo(120))
                .body("enableMetadataSigning", equalTo(true));

        String[] destinationUrls = response.jsonPath().getString("destinationURLs")
                .replace("[", "").replace("]", "").replace(" ", "").split(",");
        Assert.assertEquals(2, destinationUrls.length);
        Assert.assertTrue(Arrays.asList(destinationUrls).contains("https://localhost:9853/test/updated"));
    }

    @Test
    public void testGetPassiveSTSInboundAuthConfigs() throws XPathExpressionException {

        Response response = getResponseOfGet(PASSIVE_STS_INBOUND_AUTH_CONFIG_API_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enableRequestSigning", equalTo(false))
                .body("passiveSTSUrl",
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(context.getContextTenant().getDomain())
                                ? equalTo(PASSIVE_STS_URL_SUPER_TENANT)
                                : equalTo(PASSIVE_STS_URL_TENANT));
    }

    @Test(dependsOnMethods = {"testGetPassiveSTSInboundAuthConfigs"})
    public void testUpdatePassiveSTSInboundAuthConfigs() throws IOException {

        String body = readResource("update-passive-sts-inbound-auth-configs.json");
        Response response = getResponseOfPatch(PASSIVE_STS_INBOUND_AUTH_CONFIG_API_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(PASSIVE_STS_INBOUND_AUTH_CONFIG_API_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("enableRequestSigning", equalTo(true));
    }
}
