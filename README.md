# (Apiman) Sidekick
Sidekick is a companion micro service providing service discovery and auto-publishing for k8 services. 
At the moment Sidekick can do this only for Apiman. However it is designed to be easily extendable for any api management solution that has the ability to have new services published to it dynamically.

The way Sidekick works is by observing services deployed inside k8 cluster and looking specific labels and annotations in service's metadata.

To enable discovery on a service, it should be marked with label
```discovery.3scale.net```

Endpoint connection can then be configured by these annotation
```properties
discovery.3scale.net/path  # Path portion api endpoint
discovery.3scale.net/scheme # scheme http/https
discovery.3scale.net/port # port (defaults to 80 or 443)
```

For Apiman gateway specifically annotation pointing to config map with policy configuration can be used
```properties
discovery.apiman.net/policies # defaults to <service-name>-policy-config
```

## How to build
First build the distribution zip.

```bash
gradle assembleShadowDist
```

### Docker image

To build the dockerized distribution of Sidekick simply build the zip as described above and then execute the following commands
```bash
cd docker/sidekick
./build.sh -n <image name>
```
If you wish to also publish the image to docker hub change the commands slightly

```bash
docker login 
cd docker/sidekick
./build.sh -n <image name> -p
```

## Running the service
```bash
unzip build/distributions/sidekick-shadow.zip
./sidekick-shadow/bin/sidekick -Dconfig.file=path/to/your/custom/sidekick.conf
```
## Openshift deployment
Even though it is perfectly fine and possible to run Sidekick directly from the distribution, it is mainly intended to be deployed inside OpenShift cluster along apiman. 
It is expected for sidekick to be deployed in the same project as Apiman gateway. Sidekick was developed to play nicely with this Apiman Vert.X Gateway deployed from this [OpenShift template]()

```
oc create -f docker/sidekick/openshift/templates/sidekick.conf
oc new-app apiman-sidekicks
```

## Configuration
Sidekick uses [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) as the format of choice for its configuration. 
Please refer to the default configuration inside [application.conf](src/main/resources/application.conf) for documentation  
