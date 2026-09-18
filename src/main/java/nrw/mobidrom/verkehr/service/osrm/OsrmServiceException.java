package nrw.mobidrom.verkehr.service.osrm;

import lombok.Getter;

/**
 * Business Exception of OsrmClient service Method
 *
 * @author stni2032
 */
@Getter
public class OsrmServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int httpStatus;
    private final OsrmRestResponseCodeType responseCode;

    /**
     * @param httpStatus   HTTP Status Code from REST-Call
     * @param responseCode OsrmRestResponseCodeType from OSRM Response
     * @param message      Message from OSRM Response
     */
    public OsrmServiceException(int httpStatus, OsrmRestResponseCodeType responseCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseCode = responseCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ",httpStatus:" + httpStatus + ",responseCode:" + responseCode;
    }
}
