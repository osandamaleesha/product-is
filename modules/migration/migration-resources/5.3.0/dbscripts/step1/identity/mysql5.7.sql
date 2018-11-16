ALTER TABLE IDN_OAUTH_CONSUMER_APPS
ADD COLUMN APP_STATE VARCHAR(25) DEFAULT 'ACTIVE';

ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN DROP IDX_IOAT_AT CON_APP_KEY;
CREATE INDEX IDX_AT ON IDN_OAUTH2_ACCESS_TOKEN(ACCESS_TOKEN);

ALTER TABLE SP_APP
ADD COLUMN ENABLE_AUTHORIZATION CHAR(1) DEFAULT '0';

ALTER TABLE SP_INBOUND_AUTH
ADD COLUMN INBOUND_CONFIG_TYPE VARCHAR(255) NOT NULL;

ALTER TABLE SP_CLAIM_MAPPING
ADD COLUMN IS_MANDATORY VARCHAR(128) DEFAULT '0';

ALTER TABLE SP_PROVISIONING_CONNECTOR
ADD COLUMN RULE_ENABLED CHAR(1) NOT NULL DEFAULT '0';

ALTER TABLE IDP_PROVISIONING_CONFIG
ADD COLUMN IS_RULES_ENABLED CHAR(1) NOT NULL DEFAULT '0';

CREATE TABLE IF NOT EXISTS IDN_RECOVERY_DATA (
  USER_NAME      VARCHAR(255) NOT NULL,
  USER_DOMAIN    VARCHAR(127) NOT NULL,
  TENANT_ID      INTEGER               DEFAULT -1,
  CODE           VARCHAR(255) NOT NULL,
  SCENARIO       VARCHAR(255) NOT NULL,
  STEP           VARCHAR(127) NOT NULL,
  TIME_CREATED   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  REMAINING_SETS VARCHAR(2500)         DEFAULT NULL,
  PRIMARY KEY (USER_NAME, USER_DOMAIN, TENANT_ID, SCENARIO, STEP),
  UNIQUE (CODE)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_PASSWORD_HISTORY_DATA (
  ID           INTEGER      NOT NULL AUTO_INCREMENT,
  USER_NAME    VARCHAR(255) NOT NULL,
  USER_DOMAIN  VARCHAR(127) NOT NULL,
  TENANT_ID    INTEGER               DEFAULT -1,
  SALT_VALUE   VARCHAR(255),
  HASH         VARCHAR(255) NOT NULL,
  TIME_CREATED TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID),
  UNIQUE (USER_NAME, USER_DOMAIN, TENANT_ID, SALT_VALUE, HASH)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_CLAIM_DIALECT (
  ID          INTEGER      NOT NULL AUTO_INCREMENT,
  DIALECT_URI VARCHAR(255) NOT NULL,
  TENANT_ID   INTEGER      NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT DIALECT_URI_CONSTRAINT UNIQUE (DIALECT_URI, TENANT_ID)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_CLAIM (
  ID         INTEGER      NOT NULL AUTO_INCREMENT,
  DIALECT_ID INTEGER,
  CLAIM_URI  VARCHAR(255) NOT NULL,
  TENANT_ID  INTEGER      NOT NULL,
  PRIMARY KEY (ID),
  FOREIGN KEY (DIALECT_ID) REFERENCES IDN_CLAIM_DIALECT (ID)
    ON DELETE CASCADE,
  CONSTRAINT CLAIM_URI_CONSTRAINT UNIQUE (DIALECT_ID, CLAIM_URI, TENANT_ID)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_CLAIM_MAPPED_ATTRIBUTE (
  ID                     INTEGER      NOT NULL AUTO_INCREMENT,
  LOCAL_CLAIM_ID         INTEGER,
  USER_STORE_DOMAIN_NAME VARCHAR(255) NOT NULL,
  ATTRIBUTE_NAME         VARCHAR(255) NOT NULL,
  TENANT_ID              INTEGER      NOT NULL,
  PRIMARY KEY (ID),
  FOREIGN KEY (LOCAL_CLAIM_ID) REFERENCES IDN_CLAIM (ID)
    ON DELETE CASCADE,
  CONSTRAINT USER_STORE_DOMAIN_CONSTRAINT UNIQUE (LOCAL_CLAIM_ID, USER_STORE_DOMAIN_NAME, TENANT_ID)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_CLAIM_PROPERTY (
  ID             INTEGER      NOT NULL AUTO_INCREMENT,
  LOCAL_CLAIM_ID INTEGER,
  PROPERTY_NAME  VARCHAR(255) NOT NULL,
  PROPERTY_VALUE VARCHAR(255) NOT NULL,
  TENANT_ID      INTEGER      NOT NULL,
  PRIMARY KEY (ID),
  FOREIGN KEY (LOCAL_CLAIM_ID) REFERENCES IDN_CLAIM (ID)
    ON DELETE CASCADE,
  CONSTRAINT PROPERTY_NAME_CONSTRAINT UNIQUE (LOCAL_CLAIM_ID, PROPERTY_NAME, TENANT_ID)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_CLAIM_MAPPING (
  ID                    INTEGER NOT NULL AUTO_INCREMENT,
  EXT_CLAIM_ID          INTEGER NOT NULL,
  MAPPED_LOCAL_CLAIM_ID INTEGER NOT NULL,
  TENANT_ID             INTEGER NOT NULL,
  PRIMARY KEY (ID),
  FOREIGN KEY (EXT_CLAIM_ID) REFERENCES IDN_CLAIM (ID)
    ON DELETE CASCADE,
  FOREIGN KEY (MAPPED_LOCAL_CLAIM_ID) REFERENCES IDN_CLAIM (ID)
    ON DELETE CASCADE,
  CONSTRAINT EXT_TO_LOC_MAPPING_CONSTRN UNIQUE (EXT_CLAIM_ID, TENANT_ID)
)
  ENGINE INNODB;

CREATE TABLE IF NOT EXISTS IDN_SAML2_ASSERTION_STORE (
  ID                            INTEGER NOT NULL AUTO_INCREMENT,
  SAML2_ID                      VARCHAR(255),
  SAML2_ISSUER                  VARCHAR(255),
  SAML2_SUBJECT                 VARCHAR(255),
  SAML2_SESSION_INDEX           VARCHAR(255),
  SAML2_AUTHN_CONTEXT_CLASS_REF VARCHAR(255),
  SAML2_ASSERTION               VARCHAR(4096),
  PRIMARY KEY (ID)
)
  ENGINE INNODB;
