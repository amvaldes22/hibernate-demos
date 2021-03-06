/* 
 * Hibernate, Relational Persistence for Idiomatic Java
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.brmeyer.demo;

import java.sql.Connection;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * @author Brett Meyer
 */
public class MultiTenancyDemo {
	private static SessionFactory sessionFactory;
	
	public static void main(String[] args) {
		final Configuration configuration = new Configuration();
		configuration.addAnnotatedClass( Project.class );
		sessionFactory = configuration.buildSessionFactory(
				new StandardServiceRegistryBuilder().build() );
		
		createTable( SimpleMultiTenantConnectionProvider.TENANT_ID_1 );
		createTable( SimpleMultiTenantConnectionProvider.TENANT_ID_2 );
		
		insertProject( SimpleMultiTenantConnectionProvider.TENANT_ID_1 );
		insertProject( SimpleMultiTenantConnectionProvider.TENANT_ID_2 );
		
		printProjects( SimpleMultiTenantConnectionProvider.TENANT_ID_1 );
		printProjects( SimpleMultiTenantConnectionProvider.TENANT_ID_2 );
		
		System.exit(0);
	}
	
	private static void createTable(String tenantId) {
		// Multi-tenancy does not currently support schema export, so manually create it here.
		try {
			final Session s = openSession( tenantId );
			final SessionImplementor sImpl = (SessionImplementor) s;
			final Connection conn = sImpl.connection();
			final Statement stmt = conn.createStatement();
			stmt.executeUpdate( "CREATE TABLE Project (id bigint generated by default as identity, name varchar(255), primary key (id))" );
			stmt.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void insertProject(String tenantId) {
		final Session s = openSession( tenantId );
		s.getTransaction().begin();
		final Project project = new Project();
		project.setName( "project_" + tenantId );
		s.persist(project);
		s.getTransaction().commit();
		s.close();
	}
	
	private static void printProjects(String tenantId) {
		final Session s = openSession( tenantId );
		s.getTransaction().begin();
		final Project project = (Project) s.createQuery( "FROM Project" ).uniqueResult();
		System.out.println(project.toString());
		s.getTransaction().commit();
		s.close();
	}
	
	private static Session openSession(String tenantId) {
		return sessionFactory.withOptions()
				.tenantIdentifier( tenantId ).openSession();
	}
}
