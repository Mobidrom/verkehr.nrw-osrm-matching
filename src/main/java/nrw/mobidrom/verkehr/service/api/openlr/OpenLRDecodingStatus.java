package nrw.mobidrom.verkehr.service.api.openlr;

/**
 * Decoding status, indicating decoding errors
 *
 * @author anqu2052
 */
public enum OpenLRDecodingStatus {
    OK,
    NO_DATA,
    BYTE_STRING_DECODING_ERROR,
    FILTERED_BY_FRC_FILTER,
    OSRM_MATCHING_FAILED,
    OSRM_RETURNED_MULTIPLE_MATCHINGS,
    OSRM_MATCHING_INPLAUSIBLE,
    APPLY_OFFSET_FAILED,
    OFFSET_ROUTING_FAILED,
    OSRM_RETURNED_U_TURN,
    INVALID_NODE_ID_ORDER,
    OSRM_RETURNED_INVALID_LEG,
    OSM_ID_DUPLICATION,
    OSRM_CONNECTION_ERROR;

}
