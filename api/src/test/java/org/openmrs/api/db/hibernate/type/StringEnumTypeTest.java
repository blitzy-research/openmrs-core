/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.type;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.usertype.DynamicParameterizedType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.OrderSet;
import org.openmrs.api.ConceptNameType;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link StringEnumType}, the custom Hibernate 7 {@code EnhancedUserType} that replaces the
 * removed {@code org.hibernate.type.EnumType}.
 * <p>
 * The type is load bearing for schema preservation: it must read and write the enum <em>name</em>
 * as a {@code VARCHAR}, exactly as the Hibernate 5 generation did. A silent switch to the ordinal,
 * or to any other SQL type, would still compile and would still round trip within a single JVM, yet
 * it would corrupt every existing row. These tests therefore assert the stored representation
 * itself, not merely that a value survives a round trip.
 * <p>
 * Coverage is deliberately in two layers. The unit layer drives the JDBC contract through mocks so
 * each branch, including both halves of the SQL {@code NULL} guard, is reachable in isolation. The
 * integration layer persists through a real session and then reads the raw column with plain SQL,
 * covering both ways the type is applied: the HBM {@code <type>} element (Obs, OrderSet) and the
 * annotation {@code @Type} (ConceptName).
 */
public class StringEnumTypeTest extends BaseContextSensitiveTest {

	private static final String ORDER_SET_DATASET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";

	private static final Integer OBS_ID = 7;

	private static final Integer ORDER_SET_ID = 2000;

	private static final Integer CONCEPT_NAME_ID = 1439;

	private SessionFactory sessionFactory;

	@BeforeEach
	public void getSessionFactory() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}

	/**
	 * Builds a configured type the way Hibernate does, through {@link DynamicParameterizedType}.
	 *
	 * @param enumClass the enum the type must map
	 * @return a type ready for use
	 */
	private StringEnumType newType(Class<? extends Enum<?>> enumClass) {
		StringEnumType type = new StringEnumType();
		Properties parameters = new Properties();
		parameters.setProperty("enumClass", enumClass.getName());
		type.setParameterValues(parameters);
		return type;
	}

	/**
	 * Reads a single string column with plain SQL on the session's own connection, so the value seen is
	 * the one Hibernate actually flushed rather than anything held in the persistence context.
	 *
	 * @param sql a query selecting exactly one string column from one row
	 * @return the column value, possibly null
	 * @throws SQLException if the query fails
	 */
	private String readSingleColumn(String sql) throws SQLException {
		Connection connection = getConnection();
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			assertTrue(resultSet.next(), "expected exactly one row for: " + sql);
			return resultSet.getString(1);
		}
	}

	/**
	 * @see StringEnumType#setParameterValues(Properties)
	 */
	@Test
	public void setParameterValues_shouldResolveTheEnumClassFromTheEnumClassParameter() {
		assertEquals(Obs.Status.class, newType(Obs.Status.class).returnedClass());
	}

	/**
	 * The annotation and HBM paths both pass {@code enumClass}, but Hibernate also supplies the entity
	 * class under {@link DynamicParameterizedType#ENTITY}, which is the documented fallback.
	 *
	 * @see StringEnumType#setParameterValues(Properties)
	 */
	@Test
	public void setParameterValues_shouldFallBackToTheEntityParameterWhenEnumClassIsAbsent() {
		StringEnumType type = new StringEnumType();
		Properties parameters = new Properties();
		parameters.setProperty(DynamicParameterizedType.ENTITY, OrderSet.Operator.class.getName());

		type.setParameterValues(parameters);

		assertEquals(OrderSet.Operator.class, type.returnedClass());
	}

	/**
	 * @see StringEnumType#setParameterValues(Properties)
	 */
	@Test
	public void setParameterValues_shouldThrowHibernateExceptionForAnUnresolvableEnumClass() {
		StringEnumType type = new StringEnumType();
		Properties parameters = new Properties();
		parameters.setProperty("enumClass", "org.openmrs.NoSuchEnumAnywhere");

		HibernateException thrown = assertThrows(HibernateException.class, () -> type.setParameterValues(parameters));

		assertTrue(thrown.getMessage().contains("org.openmrs.NoSuchEnumAnywhere"));
		assertInstanceOf(ClassNotFoundException.class, thrown.getCause());
	}

	/**
	 * VARCHAR is the schema-preservation guarantee: the column stores the enum name as text.
	 *
	 * @see StringEnumType#getSqlType()
	 */
	@Test
	public void getSqlType_shouldBeVarcharSoTheColumnStoresTheName() {
		assertEquals(Types.VARCHAR, newType(Obs.Status.class).getSqlType());
	}

	/**
	 * @see StringEnumType#equals(Enum, Enum)
	 * @see StringEnumType#hashCode(Enum)
	 */
	@Test
	public void equalsAndHashCode_shouldCompareEnumConstantsByIdentity() {
		StringEnumType type = newType(Obs.Status.class);

		assertTrue(type.equals(Obs.Status.FINAL, Obs.Status.FINAL));
		assertFalse(type.equals(Obs.Status.FINAL, Obs.Status.AMENDED));
		assertTrue(type.equals(null, null));
		assertFalse(type.equals(Obs.Status.FINAL, null));
		assertEquals(Obs.Status.FINAL.hashCode(), type.hashCode(Obs.Status.FINAL));
		assertEquals(0, type.hashCode(null));
	}

	/**
	 * @see StringEnumType#nullSafeGet(ResultSet, int, org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeGet_shouldReadTheEnumNameFromTheStringColumn() throws SQLException {
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.getString(1)).thenReturn("AMENDED");
		when(resultSet.wasNull()).thenReturn(false);

		assertEquals(Obs.Status.AMENDED, newType(Obs.Status.class).nullSafeGet(resultSet, 1, null));
	}

	/**
	 * Guards the first half of the null check. A driver may hand back a non-null placeholder for a SQL
	 * NULL, so {@code wasNull()} alone must be enough to yield null.
	 *
	 * @see StringEnumType#nullSafeGet(ResultSet, int, org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeGet_shouldReturnNullWhenWasNullReportsASqlNull() throws SQLException {
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.getString(1)).thenReturn("AMENDED");
		when(resultSet.wasNull()).thenReturn(true);

		assertNull(newType(Obs.Status.class).nullSafeGet(resultSet, 1, null));
	}

	/**
	 * Guards the second half of the null check, for drivers that return null without flagging it.
	 *
	 * @see StringEnumType#nullSafeGet(ResultSet, int, org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeGet_shouldReturnNullWhenTheColumnValueIsNull() throws SQLException {
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.getString(1)).thenReturn(null);
		when(resultSet.wasNull()).thenReturn(false);

		assertNull(newType(Obs.Status.class).nullSafeGet(resultSet, 1, null));
	}

	/**
	 * @see StringEnumType#nullSafeGet(ResultSet, int, org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeGet_shouldRejectAValueThatIsNotAConstantOfTheMappedEnum() throws SQLException {
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.getString(1)).thenReturn("NOT_A_STATUS");
		when(resultSet.wasNull()).thenReturn(false);
		StringEnumType type = newType(Obs.Status.class);

		assertThrows(IllegalArgumentException.class, () -> type.nullSafeGet(resultSet, 1, null));
	}

	/**
	 * @see StringEnumType#nullSafeSet(PreparedStatement, Enum, int,
	 *      org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeSet_shouldWriteTheEnumNameAsAString() throws SQLException {
		PreparedStatement statement = mock(PreparedStatement.class);

		newType(Obs.Status.class).nullSafeSet(statement, Obs.Status.AMENDED, 3, null);

		// the NAME, never the ordinal - this is what keeps existing rows readable
		verify(statement).setString(3, "AMENDED");
	}

	/**
	 * @see StringEnumType#nullSafeSet(PreparedStatement, Enum, int,
	 *      org.hibernate.type.descriptor.WrapperOptions)
	 */
	@Test
	public void nullSafeSet_shouldWriteAVarcharNullForANullEnum() throws SQLException {
		PreparedStatement statement = mock(PreparedStatement.class);

		newType(Obs.Status.class).nullSafeSet(statement, null, 3, null);

		verify(statement).setNull(3, Types.VARCHAR);
	}

	/**
	 * @see StringEnumType#deepCopy(Enum)
	 * @see StringEnumType#isMutable()
	 */
	@Test
	public void deepCopy_shouldReturnTheSameConstantBecauseEnumsAreImmutable() {
		StringEnumType type = newType(Obs.Status.class);

		assertSame(Obs.Status.FINAL, type.deepCopy(Obs.Status.FINAL));
		assertNull(type.deepCopy(null));
		assertFalse(type.isMutable());
	}

	/**
	 * The second level cache stores the disassembled form, so it must be the name string rather than
	 * the enum instance for the cached value to survive a JVM restart.
	 *
	 * @see StringEnumType#disassemble(Enum)
	 * @see StringEnumType#assemble(Serializable, Object)
	 */
	@Test
	public void disassembleAndAssemble_shouldRoundTripThroughTheEnumName() {
		StringEnumType type = newType(Obs.Status.class);

		Serializable disassembled = type.disassemble(Obs.Status.AMENDED);

		assertEquals("AMENDED", disassembled);
		assertSame(Obs.Status.AMENDED, type.assemble(disassembled, null));
		assertNull(type.disassemble(null));
		assertNull(type.assemble(null, null));
	}

	/**
	 * @see StringEnumType#toSqlLiteral(Enum)
	 */
	@Test
	public void toSqlLiteral_shouldQuoteTheEnumName() {
		StringEnumType type = newType(Obs.Status.class);

		assertEquals("'AMENDED'", type.toSqlLiteral(Obs.Status.AMENDED));
		assertEquals("null", type.toSqlLiteral(null));
	}

	/**
	 * @see StringEnumType#toString(Enum)
	 */
	@Test
	public void toString_shouldReturnTheEnumName() {
		StringEnumType type = newType(Obs.Status.class);

		assertEquals("AMENDED", type.toString(Obs.Status.AMENDED));
		assertNull(type.toString(null));
	}

	/**
	 * @see StringEnumType#fromStringValue(CharSequence)
	 */
	@Test
	public void fromStringValue_shouldParseTheEnumNameFromAnyCharSequence() {
		StringEnumType type = newType(Obs.Status.class);

		assertSame(Obs.Status.AMENDED, type.fromStringValue("AMENDED"));
		// a non String CharSequence proves the implementation converts before parsing
		assertSame(Obs.Status.FINAL, type.fromStringValue(new StringBuilder("FINAL")));
		assertNull(type.fromStringValue(null));
	}

	/**
	 * Builds a persistable Obs that reuses the person and concept of an existing row. Obs is
	 * deliberately immutable apart from its void related fields (see {@code ImmutableObsInterceptor},
	 * whose mutable set is voided, dateVoided, voidedBy, voidReason and groupMembers), so an enum value
	 * can only ever reach the obs table through an INSERT. That is how OpenMRS itself records an
	 * amendment, and it is therefore the only path this type takes for Obs in production.
	 *
	 * @param session the session used to resolve the referenced person and concept
	 * @param status the status to persist
	 * @param interpretation the interpretation to persist, may be null
	 * @return an unsaved Obs
	 */
	private Obs newObs(Session session, Obs.Status status, Obs.Interpretation interpretation) {
		Obs existing = session.find(Obs.class, OBS_ID);
		assertNotNull(existing);
		Obs obs = new Obs();
		obs.setPerson(existing.getPerson());
		obs.setConcept(existing.getConcept());
		obs.setObsDatetime(new Date());
		obs.setStatus(status);
		obs.setInterpretation(interpretation);
		return obs;
	}

	/**
	 * Round trips the two HBM mapped Obs enums through a real session and verifies the stored text. The
	 * reload happens after {@code clear()}, so the value comes from the database rather than the
	 * persistence context.
	 *
	 * @see StringEnumType
	 */
	@Test
	public void obsEnums_shouldRoundTripAsTheEnumNameInTheVarcharColumns() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		Obs obs = newObs(session, Obs.Status.AMENDED, Obs.Interpretation.CRITICALLY_ABNORMAL);

		session.persist(obs);
		session.flush();

		Integer obsId = obs.getObsId();
		assertNotNull(obsId);
		assertEquals("AMENDED", readSingleColumn("select status from obs where obs_id = " + obsId));
		assertEquals("CRITICALLY_ABNORMAL", readSingleColumn("select interpretation from obs where obs_id = " + obsId));

		session.clear();
		Obs reloaded = session.find(Obs.class, obsId);
		assertNotSame(obs, reloaded);
		assertEquals(Obs.Status.AMENDED, reloaded.getStatus());
		assertEquals(Obs.Interpretation.CRITICALLY_ABNORMAL, reloaded.getInterpretation());
	}

	/**
	 * A null nullable enum must reach the column as a real SQL NULL, not the text "null" and not an
	 * empty string. This is the persisted counterpart of the {@code setNull(index, VARCHAR)} branch,
	 * and the populated sibling inserted alongside it keeps the two branches distinguishable.
	 *
	 * @see StringEnumType
	 */
	@Test
	public void obsInterpretation_shouldRoundTripANullAsASqlNull() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		Obs withInterpretation = newObs(session, Obs.Status.FINAL, Obs.Interpretation.NEGATIVE);
		Obs withoutInterpretation = newObs(session, Obs.Status.FINAL, null);

		session.persist(withInterpretation);
		session.persist(withoutInterpretation);
		session.flush();

		assertEquals("NEGATIVE",
		    readSingleColumn("select interpretation from obs where obs_id = " + withInterpretation.getObsId()));
		assertNull(readSingleColumn("select interpretation from obs where obs_id = " + withoutInterpretation.getObsId()));

		session.clear();
		assertEquals(Obs.Interpretation.NEGATIVE,
		    session.find(Obs.class, withInterpretation.getObsId()).getInterpretation());
		assertNull(session.find(Obs.class, withoutInterpretation.getObsId()).getInterpretation());
	}

	/**
	 * The third HBM mapped consumer of the type.
	 *
	 * @see StringEnumType
	 */
	@Test
	public void orderSetOperator_shouldRoundTripAsTheEnumNameInTheVarcharColumn() throws SQLException {
		executeDataSet(ORDER_SET_DATASET);
		Session session = sessionFactory.getCurrentSession();
		OrderSet orderSet = session.find(OrderSet.class, ORDER_SET_ID);
		assertNotNull(orderSet);
		assertEquals(OrderSet.Operator.ALL, orderSet.getOperator());

		orderSet.setOperator(OrderSet.Operator.ANY);
		session.flush();

		assertEquals("ANY", readSingleColumn("select operator from order_set where order_set_id = " + ORDER_SET_ID));

		session.clear();
		assertEquals(OrderSet.Operator.ANY, session.find(OrderSet.class, ORDER_SET_ID).getOperator());
	}

	/**
	 * ConceptName reaches the same type through Hibernate's {@code @Type} annotation rather than an HBM
	 * {@code <type>} element, so this covers the second of the two wiring paths.
	 *
	 * @see StringEnumType
	 */
	@Test
	public void conceptNameType_shouldRoundTripAsTheEnumNameForTheAnnotationMappedPath() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		ConceptName conceptName = session.find(ConceptName.class, CONCEPT_NAME_ID);
		assertNotNull(conceptName);
		assertEquals(ConceptNameType.FULLY_SPECIFIED, conceptName.getConceptNameType());

		conceptName.setConceptNameType(ConceptNameType.INDEX_TERM);
		session.flush();

		assertEquals("INDEX_TERM",
		    readSingleColumn("select concept_name_type from concept_name where concept_name_id = " + CONCEPT_NAME_ID));

		session.clear();
		assertEquals(ConceptNameType.INDEX_TERM, session.find(ConceptName.class, CONCEPT_NAME_ID).getConceptNameType());
	}
}
