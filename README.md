# xsample-server
Server component of the XSample workflow for creating excerpts from text corpora hosted in a DataVerse repository.


## Getting Started

XSample is coded for a Java 8 environment. Since the build script uses Gradle 7, Java 11 or newer is needed for building (or deploying, if done via Gradle, see below).

### Server Setup

XSample is currently targeted towards deployment on a Payara 5 server instance (tested with Payara 5.2020.7). Other JSF environments can be used, such as Tomcat, but the configuration needs to be adjusted for those situations.

When deploying XSample on a production system, the `web.xml` in `src/main/webapp/WEB-INF` should be modified to set the `javax.faces.PROJECT_STAGE` parameter from `Development` to `Production` before building the WAR. In addition, the persistence-unit requires a database with the associated JDBS cnnection pool named `xsample`. For development and testing PostgreSQL 9.6 has been used, but you can configure the SQL backend of your choice and [plug it into](https://docs.payara.fish/community/docs/documentation/user-guides/connection-pools/connection-pools.html) Payara.

For the actual deyployment step you are also free to use the Payara admin UI to manually add the WAR or deploy via Gradle directly with the cargo plugin. To use the latter, a copy of the `deployment.ini.dummy` in the project root folder should be created and renamed to `deployment.ini`. Inside the `deployment.ini` you then define the parameters for deploying XSample on either a local or remote Payara instance. A series of additional Gradle tasks are now available to (un)deploy XSample, all of which are documented on the plugin's [README](https://github.com/bmuschko/gradle-cargo-plugin). 

With all of the above done, XSample can be easily managed:
- `gradle cargoDeployRemote` - for initial deployment
- `gradle cargoRedeployRemote` - for redeploying XSample
- `gradle cargoUndeployRemote` - to undeploy from the remote container

### Dataverse Integration

Once the XSample server is up, it needs to be registered as an `external tool` with the desired [Dataverse](https://dataverse.org/) instance. First create a JSON manifest (e.g. `xsample.json`) on the server describing the XSample tool for Dataverse (a documentation of the manifest content is available [here](https://guides.dataverse.org/en/latest/api/external-tools.html)): 

```json
{
  "displayName": "XSample",
  "description": "Generate excerpts from a text corpus via XSample.",
  "scope": "file",
  "type": "explore",
  "contentType": "application/json",
  "toolUrl": "https://my.server/xsample",
  "toolParameters": {
    "queryParameters": [
      {
        "file": "{fileId}"
      },
      {
        "key": "{apiToken}"
      },
      {
        "site": "{siteUrl}"
      }
    ]
  }
}
```

The `toolUrl` attribute should be adjusted to point to the running XSample server.

When done, use the Dataverse [admin API](https://guides.dataverse.org/en/latest/admin/external-tools.html) to add XSample to the repository's inventory of external tools. The following assumes that Payara is serving to the default port of 8080 on the server. If your setup is different, adjust the port accordingly before running the command on the server:

`curl -X POST -H 'Content-type: application/json' http://localhost:8080/api/admin/externalTools --upload-file xsample.json`

Your Dataverse instance should now display `XSample` as an aditional explore option on JSON files.

### A Note on External Tools and File Types

Dataverse uses MIME types for selecting which external tools to display for a given file. Unfortunately the list of recognized types is rather limited and not easily extended, since many UI components also rely on it. Therefore XSample has to be registered for the general JSON type (`application/json`) instead of a custom subtype. As a result Dataverse will produce a lot of false positives and display the `XSample` option for any JSON file it encounters.
