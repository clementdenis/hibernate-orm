/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.envers.integration.strategy;

import static org.hibernate.testing.junit4.ExtraAssertions.assertTyping;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.configuration.Configuration;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.envers.strategy.AuditStrategy;
import org.hibernate.envers.strategy.DefaultAuditStrategy;
import org.hibernate.envers.strategy.ValidityAuditStrategy;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.testing.jdbc.SharedDriverManagerConnectionProviderImpl;
import org.junit.Test;

import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.TestForIssue;

/**
 * A set of unit tests that verify that the audit strategy selector appropriately selects and
 * creators the correct strategy class.
 *
 * @author Chris Cranford
 */
@TestForIssue( jiraKey = "HHH-12077" )
public class AuditStraegySelectorTest {
    
	@Test
	public void testAuditStrategySelectorNoneSpecified() {
		testAuditStrategySelector( null, DefaultAuditStrategy.class );
	}

	@Test
	public void testAuditStrategySelectorDefaultSpecified() {
        // test old implementations
		testAuditStrategySelector( "default", DefaultAuditStrategy.class );
		testAuditStrategySelector( DefaultAuditStrategy.class.getSimpleName(), DefaultAuditStrategy.class );
		testAuditStrategySelector( DefaultAuditStrategy.class.getName(), DefaultAuditStrategy.class );

        // test new implementation
        testAuditStrategySelector(
                org.hibernate.envers.strategy.internal.DefaultAuditStrategy.class.getName(),
                org.hibernate.envers.strategy.internal.DefaultAuditStrategy.class
        );
	}

	@Test
	public void testAuditStrategySelectorValiditySpecified() {
        // test old implementations
		testAuditStrategySelector( "validity", ValidityAuditStrategy.class );
		testAuditStrategySelector( ValidityAuditStrategy.class.getSimpleName(), ValidityAuditStrategy.class );
		testAuditStrategySelector( ValidityAuditStrategy.class.getName(), ValidityAuditStrategy.class );

        // test new implementation
        testAuditStrategySelector(
                org.hibernate.envers.strategy.internal.ValidityAuditStrategy.class.getName(),
                org.hibernate.envers.strategy.internal.ValidityAuditStrategy.class
        );
	}

	private void testAuditStrategySelector(String propertyValue, Class<? extends AuditStrategy> expectedStrategyClass) {
		final Map<String, Object> properties = new HashMap<>();
		if ( propertyValue != null ) {
			properties.put( EnversSettings.AUDIT_STRATEGY, propertyValue );
		}

		properties.put(AvailableSettings.CONNECTION_PROVIDER, SharedDriverManagerConnectionProviderImpl.getInstance() );

		final ServiceRegistry sr = ServiceRegistryBuilder.buildServiceRegistry( properties );
		try {
			final MetadataImplementor metadata = (MetadataImplementor) new MetadataSources( sr ).buildMetadata();
            final Configuration configuration = sr.getService( EnversService.class ).getConfig();
			assertTyping( expectedStrategyClass, configuration.getAuditStrategy() );
		}
		finally {
			ServiceRegistryBuilder.destroy( sr );
		}
	}    
}
