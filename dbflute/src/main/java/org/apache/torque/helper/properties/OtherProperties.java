package org.apache.torque.helper.properties;

import java.util.Properties;

/**
 * Build properties for Torque.
 * 
 * @author mkubo
 */
public final class OtherProperties extends AbstractHelperProperties {

    //    private static final Log _log = LogFactory.getLog(GeneratedClassPackageProperties.class);

    /**
     * Constructor.
     */
    public OtherProperties(Properties prop) {
        super(prop);
    }

    // ===============================================================================
    //                                                              Properties - Other
    //                                                              ==================
    public boolean isStopGenerateExtendedBhv() {
        return booleanProp("torque.isStopGenerateExtendedBhv", false);
    }
    
    public boolean isStopGenerateExtendedDao() {
        return booleanProp("torque.isStopGenerateExtendedDao", false);
    }

    public boolean isStopGenerateExtendedEntity() {
        return booleanProp("torque.isStopGenerateExtendedEntity", false);
    }

}