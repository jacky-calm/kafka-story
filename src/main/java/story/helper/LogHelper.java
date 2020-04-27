package story.helper;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LogHelper {
    static {
        BasicConfigurator.configure();
    }

    static Logger logger = Logger.getLogger(LogHelper.class);

    public static Logger getLogger(Class clazz) {
        return Logger.getLogger(clazz);
    }

}
