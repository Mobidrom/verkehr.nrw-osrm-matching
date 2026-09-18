# verkehr.nrw-osrm-matching

`verkehr.nrw-osrm-matching` is a service for decoding
**[OpenLR](https://download.tomtom.com/open/banners/openlr-whitepaper_v1.5.pdf)** location references and resolving them to
real-world road geometries based on OpenStreetMap (OSM).

It is part of the Verkehr.NRW ecosystem and processes OpenLR-encoded traffic location references received from external systems.

To reconstruct the corresponding road geometry, the service uses **OSRM (Open Source Routing Machine)**, an open-source routing
engine built on OpenStreetMap (OSM) data. While OSM provides the underlying road network, OSRM performs the map matching and
routing required to reconstruct the intended road segment.

The service combines OpenLR decoding, OSRM-based map matching, routing, offset processing and validation to produce a validated
road geometry together with the corresponding OSM segment IDs.

This README explains how the matching process works, how to run the service locally, and how to integrate it into other
applications.

---

## 1. Overview

This module converts **OpenLR-encoded location references** into **real-world road geometries (OSM)** using OSRM-based map
matching and routing.

OpenLR is a compact, map-independent format for describing road locations. Instead of storing the complete road geometry, it
encodes a road location using a small set of reference points and additional metadata such as direction, distance and road class.

The purpose of this module is to reconstruct the corresponding road geometry in the OpenStreetMap (OSM) road network.

To achieve this, the module uses **OSRM (Open Source Routing Machine)**, a routing engine that operates on OSM data. While **OSM**
provides the underlying road network (roads, intersections and map geometry), **OSRM** performs the map matching and routing
needed to identify the most likely road segment.

The decoding pipeline consists of the following steps:

- **OpenLR decoding** transforms the encoded input into structured Location Reference Points (LRPs).
- **OSRM map matching and routing** matches these reference points to the OSM road network and reconstructs the most likely route.
- **Offset processing** applies the start and end offsets defined by the OpenLR specification.
- **Validation** verifies that the reconstructed route is plausible before returning the final result.

The final output consists of a validated `LineString` geometry, the corresponding OSM IDs, and a detailed decoding status
(`OpenLRDecodingStatus`).

---

## 2. What This Module Does

This module takes a compact OpenLR-encoded location and transforms it into a **real-world road representation** based on OSM data.

---

### Input

The decoder accepts the following inputs:

#### `openlrBase64`

A Base64-encoded OpenLR location reference.

This is the primary input of the matching process. During decoding, it is converted into a binary representation (`ByteString`)
and transformed into a structured `RawLocationReference`, which is then used for map matching and routing.

#### `realLength` *(optional)*

The expected length of the road segment in meters.

If provided, this value is used during validation instead of the length calculated from the decoded OpenLR reference. This
improves plausibility checks when an externally known segment length is available.

---

### Output

- `geometry` (LineString)  
  The reconstructed road geometry as a continuous path on the map.  
  This represents the actual shape of the road in geographic coordinates.

- `osmIds` (number[])  
  An ordered list of OSM segment (way/node) IDs that make up the matched route.  
  These IDs allow linking the result back to the underlying map data.

- `status` (`OpenLRDecodingStatus`)  
  A detailed result status describing whether the decoding and matching process was successful or why it failed (e.g. no match,
  implausible route, offset error).

## 3. Step-by-Step Explanation

### 3.1 Binary Decoding

The OpenLR Base64 string is decoded into a structured object:

→ `RawLocationReference`

This object contains a sequence of **Location Reference Points (LRPs)**, which describe the road in an abstract way.

Each LRP includes:

- **Coordinates (lat/lon)** → approximate position of the road

- **Bearing (direction)** → direction of the road at that point

- **Distance to next point** → length of the segment between LRPs

- **Functional Road Class (FRC)** → type of road (e.g. highway, main road)

Together, these attributes form a **logical description of a road**, but not an exact geometry.

At this stage:

- no real road segments are known
- no OSM IDs are available
- no exact path is defined

The LRPs only provide enough information to later reconstruct the real road using map matching.

### 3.2 OSRM Map Matching and Route Reconstruction

After the OpenLR input has been decoded into a `RawLocationReference`, the resulting Location Reference Points (LRPs) are passed
to the OSRM `/match` API.

OSRM first aligns the LRPs with the OpenStreetMap (OSM) road network (map matching). Since the LRPs describe only reference points
and not the complete road geometry, OSRM then reconstructs the most likely drivable route connecting these points along the road
network.

The reconstructed route is represented as a continuous `LineString` and forms the basis for all subsequent processing steps.

The matching result contains:

- a continuous `LineString` geometry representing the reconstructed route
- an ordered list of traversed OSM road segments
- trace points showing where each LRP was matched to the road network

The reconstructed route is then passed to the offset processing and validation steps to produce the final OpenLR geometry.

### 3.3 Offset Processing

Start and end offsets are part of the OpenLR specification.

They allow the creator of an OpenLR location to specify that the described road segment does not begin exactly at the first
Location Reference Point (LRP) or end exactly at the last LRP.

Instead, the actual location starts or ends at a defined distance from these reference points.

After OSRM has reconstructed the corresponding route, the decoder applies these offsets to the matched result.

This includes adjusting:

- the reconstructed `LineString`
- the ordered list of OSM segment IDs
- the route distances returned by OSRM

To do this, the decoder traverses the reconstructed route, accumulates the distances between the matched OSM nodes and determines
the exact positions where the start and end offsets must be applied. If an offset lies within a line segment, a new geometry point
is interpolated at the correct position.

The result is a geometry that represents exactly the location described by the OpenLR reference.

### 3.4 Validation

The result is validated to ensure plausibility.

Instead of requiring exact matches, the system uses **tolerance-based checks** to handle real-world inaccuracies.

---

### Checks include:

- **Length comparison**  
  → The length of the matched route is compared to the expected length from OpenLR.  
  A deviation within a defined tolerance (e.g. several meters) is accepted.

- **Trace distance**  
  → The distance between LRPs and the matched geometry is measured.  
  Small deviations are allowed to account for map inaccuracies and matching uncertainty.

- **Structural consistency**  
  → The resulting path must be continuous and valid:
    - connected segments
    - correct node order
    - no invalid or broken paths

---

### Important

The validation is **not strict equality-based**, but **heuristic and tolerance-driven**.

This ensures that realistic results are accepted even if the input data or matching is slightly imprecise.

## 4. OSRM API Usage

The system uses the **OSRM `/match` API** to perform map matching.

This API takes a sequence of coordinates (derived from the OpenLR LRPs) and matches them to the most likely road segments in the
OSM network.

---

### What `/match` does

- Snaps input points to nearby roads
- Evaluates multiple possible paths
- Returns the most likely route based on:
    - distance
    - direction (bearing)
    - connectivity

---

### How it is used in this system

- The decoded LRPs are converted into coordinates
- These coordinates are sent to the OSRM `/match` endpoint
- OSRM returns:
    - a matched route (geometry)
    - a sequence of OSM node/way IDs

This result is then used as the basis for further processing (offsets, validation).

## 5. OSRM Matching Configuration

### Matching Parameters

The system uses different parameter configurations when calling the OSRM `/match` API.  
These parameters influence how the input points (LRPs) are matched to the road network.

---

- `radius`  
  Defines how far OSRM is allowed to search around each input point.

  A larger radius:
    - increases the chance of finding a match
    - but may introduce incorrect candidates

  A smaller radius:
    - enforces stricter matching
    - but may lead to no match if the input is imprecise

---

- `bearings`  
  Adds direction information to each point based on the OpenLR input.

  This helps OSRM prefer road segments that match the expected direction and  
  reduces the likelihood of incorrect matches (e.g. wrong driving direction).

---

- `timestamps`  
  Artificial timestamps are generated for the input points.

  These values help OSRM interpret the sequence of points as a continuous path  
  and improve the consistency of the matching result.

---

- `overview=full`  
  Requests the complete geometry of the matched route.

  This ensures that:
    - the full LineString is returned
    - the geometry can be used for further processing (offsets, validation)

---

### Multi-Parameter Strategy

The system performs multiple matching attempts using different parameter configurations.

Typical approach:

- **initial attempt with stricter parameters**  
  → focuses on accuracy

- **fallback attempts with more relaxed parameters**  
  → increase the chance of finding a valid match

---

### Why this is important

OpenLR input can be:

- imprecise
- slightly shifted from the actual road
- incomplete

By adjusting parameters and retrying the matching process, the system improves robustness and is able to handle a wide range of
input quality. The result of the process is described by `OpenLRDecodingStatus`.

### Success

- `OK`  
  → decoding and matching successful

---

### Input / Data Errors

- `NO_DATA`  
  → No OpenLR input was provided.

- `BYTE_STRING_DECODING_ERROR`  
  → The Base64 input could not be decoded into a valid OpenLR structure.  
  Possible cause: invalid or corrupted input.

- `FILTERED_BY_FRC_FILTER`  
  → The location was filtered out based on Functional Road Class (FRC).  
  Possible cause: road class does not match expected criteria.

---

### OSRM Matching Errors

- `OSRM_MATCHING_FAILED`  
  → No valid route could be found by OSRM.  
  Possible cause: coordinates too far from road network or inconsistent LRPs.

- `OSRM_RETURNED_MULTIPLE_MATCHINGS`  
  → Multiple possible routes were returned.  
  Possible cause: ambiguous input or overlapping roads.

- `OSRM_MATCHING_INPLAUSIBLE`  
  → A route was found but failed validation checks.  
  Possible cause: large deviation in length or geometry.

- `OSRM_RETURNED_U_TURN`  
  → The route contains an invalid U-turn.  
  Possible cause: incorrect direction or noisy input data.

- `OSRM_RETURNED_INVALID_LEG`  
  → OSRM returned a broken or invalid route segment.  
  Possible cause: routing inconsistency between points.

- `INVALID_NODE_ID_ORDER`  
  → The sequence of nodes is inconsistent or not connected properly.  
  Possible cause: matching errors or invalid routing result.

- `OSM_ID_DUPLICATION`  
  → Duplicate OSM IDs were detected in the route.  
  Possible cause: loop or incorrect path reconstruction.

---

### Offset Errors

- `APPLY_OFFSET_FAILED`  
  → The geometry could not be correctly trimmed using offsets.  
  Possible cause: offset larger than route length or geometry issues.

- `OFFSET_ROUTING_FAILED`  
  → Routing failed during offset processing.  
  Possible cause: missing or inconsistent geometry.

---

### System Errors

- `OSRM_CONNECTION_ERROR`  
  → The OSRM service could not be reached.  
  Possible cause: network issues or OSRM service unavailable.

### Purpose

These status values help with:

- debugging
- monitoring
- quality control

---

## 6. Running the Matching Locally

To run this module locally, the following components are required:

### Requirements

To run the matching service locally, the following components are required:

- a running OSRM backend exposing the `/match` endpoint
- an available OSRM routing profile (e.g. `driving`)

The OSRM backend must already be prepared with OpenStreetMap (OSM) routing data. How this data is imported and how the OSRM
instance is built is outside the scope of this project and is covered by the OSRM documentation.

---

## 7. Getting Started (Local Setup)

This section describes the minimum setup required to run the OpenLR matching service locally.

### Prerequisites

The OpenLR matching module requires:

- Java and Gradle
- PostgreSQL database
- OSRM backend providing the `/match` endpoint
- (Optional) Docker / Docker Compose

---

### 1. Build the Project

Run the initial Gradle build to generate all required classes.

```bash
./gradlew -x test build
```

---

### 2. Database Setup

#### Development Mode

When running in development mode, the application requires access to a PostgreSQL database.

Configure the database connection in `src/main/resources/application-dev.properties` according to your local environment.

Start the application with:

```bash
./gradlew quarkusDev
```

For tests, Quarkus automatically starts an isolated Testcontainer database. By default, it uses a random port, but this can be
configured via:

```properties
quarkus.datasource.devservices.port=<port>
```

---

#### Production Mode

For production, the database and osrm connection is configured using environment variables.

Create a file named:

```text
env.docker
```

Example:

```env
DB_HOST=
DB_PORT=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=
# osrm endpoints, set the other one to something like http://localhost if you only want to use one
# osrm endpoint with car profile
OSRM_OPENLR_ROUTER_HOST=
# osrm endpoint with car profile changed to weight_name = 'distance', used to find a better fitting route
OSRM_DISTANCE_ROUTER_HOST=
```

Build the application:

```bash
./gradlew build
```

Run:

```bash
set -a && source env.docker && set +a && java -jar build/quarkus-app/quarkus-run.jar
```

Alternatively, build an uber-jar:

```bash
./gradlew build -Dquarkus.package.type=uber-jar
```

and start it with:

```bash
set -a && source env.docker && set +a && java -jar build/*-runner.jar
```

---

### 3. Running with Docker

Build the Docker image `ghcr.io/mobidrom/verkehr.nrw-osrm-matching/osrm-matching:local`

```bash
./gradlew build \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.tag="local" \
  -Dquarkus.container-image.labels."org.opencontainers.image.source"="https://github.com/mobidrom/verkehr.nrw-osrm-matching"
```

Run the Docker Container:

```bash
docker run -d --rm \
    --env-file env.docker \
    -p 8085:8085 \
    --name osrm-matching \
    ghcr.io/mobidrom/verkehr.nrw-osrm-matching/osrm-matching:local
```

The service will then be available at:

```text
http://127.1.1.0:8085
```

---

### 4. Configure the OSRM Backend

The matching service requires a running **OSRM backend** exposing the `/match` endpoint.

There is no official public OSRM endpoint intended for production use. Therefore, a local or self-hosted OSRM instance must be
configured.

Configure the endpoint via the application configuration:

```properties
osrm.matching.service.host=http://osrm-matching:8085
```

### 5. Running the Matching

Once the application, database and OSRM backend are running, the matching module can process OpenLR requests.

For every request, the system performs the following steps:

1. Decode the OpenLR Base64 input.
2. Convert it into a `RawLocationReference`.
3. Call the OSRM `/match` endpoint.
4. Apply routing, offset processing and validation.
5. Return:
    - reconstructed `LineString` geometry
    - ordered OSM IDs
    - decoding status (`OpenLRDecodingStatus`)

---

### Summary

To run the matching service locally, the following components are required:

- PostgreSQL database
- Running OSRM backend exposing the `/match` endpoint
- Correct application configuration (`osrm.matching.service.host`)
- Java / Gradle
- Docker (optional, recommended)

---

## License

This project is licensed under the **European Union Public Licence v1.2 (EUPL-1.2)**. See the [LICENSE](LICENSE) file for the full
license text.

The EUPL is an open-source license created and approved by the European Commission. It is specifically designed for software
published by public administrations and is compatible with several widely used open-source licenses (including GPL, LGPL, MPL, and
Apache 2.0 under its compatibility clause).

For more information, see the official EUPL page:
https://joinup.ec.europa.eu/collection/eupl

---

## Data Attribution

This project uses map data from **[OpenStreetMap](https://www.openstreetmap.org/)**, licensed under the
**[Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/)**.

© OpenStreetMap contributors

Routing and map matching are performed using **[OSRM (Open Source Routing Machine)](https://project-osrm.org/)**, which is
licensed under the **[BSD 2-Clause License](https://github.com/Project-OSRM/osrm-backend/blob/master/LICENSE.TXT)**.

---

## Contributing

Contributions are welcome!

If you would like to contribute, please open an issue or submit a pull request. For larger changes, we recommend opening an issue
first to discuss the proposed changes.

---

## Contact

Maintained by **[NRW.Mobidrom GmbH](https://www.mobidrom.nrw/)** ([GitHub](https://github.com/Mobidrom)).

For questions or issues, please open a GitHub Issue or contact [mobi@mobidrom.nrw](mailto:mobi@mobidrom.nrw).

