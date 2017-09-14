package edu.sc.seis.sod.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;

public class CustomSessionFactoryObserver implements SessionFactoryObserver {

    @Override
    public void sessionFactoryCreated(SessionFactory factory) {
        logger.debug("sessionFactoryCreated");
    }

    @Override
    public void sessionFactoryClosing(SessionFactory factory) {
        logger.debug("sessionFactoryClosing");
    }

    @Override
    public void sessionFactoryClosed(SessionFactory factory) {
        logger.debug("sessionFactoryClosed");
    }

    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomSessionFactoryObserver.class);
}
