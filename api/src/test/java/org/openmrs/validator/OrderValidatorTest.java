/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.CareSetting;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods on the {@link OrderValidator} class.
 */
public class OrderValidatorTest extends BaseContextSensitiveTest {

	private class SomeDrugOrder extends DrugOrder {}

	private OrderService orderService;

	protected static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";

	@BeforeEach
	public void setup() {
		orderService = Context.getOrderService();
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderIsNull() {
		Errors errors = new BindException(new Order(), "order");
		new OrderValidator().validate(null, errors);

		assertTrue(errors.hasErrors());
		assertEquals("error.general", errors.getAllErrors().get(0).getCode());
		// the null guard rejects globally, never against a field; DrugOrderValidator relies on this
		// being exactly one error to prove its own super.validate delegation adds a second one
		assertTrue(errors.hasGlobalErrors());
		assertEquals(1, errors.getGlobalErrorCount());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderAndEncounterHaveDifferentPatients() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("encounter"));
		assertEquals("Order.error.encounterPatientMismatch", errors.getFieldError("encounter").getCode());
		// every other required field is set, so the mismatch must be the only rejection
		assertEquals(1, errors.getErrorCount());
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedIsBeforeEncountersEncounterDatetime() {
		Date encounterDate = new Date();
		Date orderDate = DateUtils.addDays(encounterDate, -1);
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		encounter.setEncounterDatetime(encounterDate);
		Order order = new Order();
		order.setDateActivated(orderDate);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(encounter);
		order.setOrderer(Context.getProviderService().getProvider(1));
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertEquals("Order.error.encounterDatetimeAfterDateActivated", errors.getFieldError("dateActivated").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfVoidedIsNull() {
		Order order = new Order();
		order.setVoided(null);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasFieldErrors("discontinued"));
		assertTrue(errors.hasFieldErrors("voided"));
		assertEquals("error.null", errors.getFieldError("voided").getCode());
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("patient"));
		assertFalse(errors.hasFieldErrors("orderer"));
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptIsNull() {
		Order order = new Order();
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasFieldErrors("discontinued"));
		assertTrue(errors.hasFieldErrors("concept"));
		// a plain Order needs its own concept; only DrugOrders may infer it from the drug
		assertEquals("Concept.noConceptSelected", errors.getFieldError("concept").getCode());
		assertFalse(errors.hasFieldErrors("patient"));
		assertFalse(errors.hasFieldErrors("orderer"));
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasFieldErrors("discontinued"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("patient"));
		assertEquals("error.null", errors.getFieldError("patient").getCode());
		assertFalse(errors.hasFieldErrors("orderer"));
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrdererIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasFieldErrors("discontinued"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("orderer"));
		assertEquals("error.null", errors.getFieldError("orderer").getCode());
		assertFalse(errors.hasFieldErrors("patient"));
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfEncounterIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setEncounter(null);

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("encounter"));
		// a missing encounter is a null rejection, distinct from the patient-mismatch rejection
		assertEquals("error.null", errors.getFieldError("encounter").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfUrgencyIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setUrgency(null);

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("urgency"));
		// a null urgency is a null rejection, not the scheduled-date pairing rejection
		assertEquals("error.null", errors.getFieldError("urgency").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfActionIsNull() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setAction(null);

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("action"));
		assertEquals("error.null", errors.getFieldError("action").getCode());
	}

	/**
	 * @throws Exception
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedAfterDateStopped() throws Exception {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(new Date());
		OrderUtilTest.setDateStopped(order, cal.getTime());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertTrue(errors.hasFieldErrors("dateStopped"));
		// the same code is reported against both ends of the interval
		assertEquals("Order.error.dateActivatedAfterDiscontinuedDate", errors.getFieldError("dateActivated").getCode());
		assertEquals("Order.error.dateActivatedAfterDiscontinuedDate", errors.getFieldError("dateStopped").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDateActivatedAfterAutoExpireDate() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(new Date());
		order.setAutoExpireDate(cal.getTime());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertTrue(errors.hasFieldErrors("autoExpireDate"));
		// the same code is reported against both ends of the interval
		assertEquals("Order.error.dateActivatedAfterAutoExpireDate", errors.getFieldError("dateActivated").getCode());
		assertEquals("Order.error.dateActivatedAfterAutoExpireDate", errors.getFieldError("autoExpireDate").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsNullWhenUrgencyIsON_SCHEDULED_DATE() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));

		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(null);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("scheduledDate"));
		assertEquals("Order.error.scheduledDateNullForOnScheduledDateUrgency",
		    errors.getFieldError("scheduledDate").getCode());

		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.STAT);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("scheduledDate"));
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfScheduledDateIsSetAndUrgencyIsNotSetAsON_SCHEDULED_DATE() {
		Order order = new Order();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));

		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ROUTINE);
		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("urgency"));
		// distinct from the error.null rejection raised when urgency is absent altogether
		assertEquals("Order.error.urgencyNotOnScheduledDate", errors.getFieldError("urgency").getCode());

		order.setScheduledDate(new Date());
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("urgency"));
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderTypejavaClassDoesNotMatchOrderclass() {
		Order order = new DrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Test order"));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("orderType"));
		assertTrue(Arrays.asList(errors.getFieldError("orderType").getCodes())
		        .contains("Order.error.orderTypeClassMismatchesOrderClass"));
		// the resolvable code itself, not merely its presence among the generated code variants
		assertEquals("Order.error.orderTypeClassMismatchesOrderClass", errors.getFieldError("orderType").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfTheClassOfTheOrderIsASubclassOfOrderTypejavaClass() {
		SomeDrugOrder order = new SomeDrugOrder();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setPatient(Context.getPatientService().getPatient(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("orderType"));
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
		Order order = new DrugOrder();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotAllowAFutureDateActivated() {
		Patient patient = Context.getPatientService().getPatient(7);
		TestOrder order = new TestOrder();
		order.setPatient(patient);
		order.setOrderType(orderService.getOrderTypeByName("Test order"));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(orderService.getCareSetting(1));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		order.setDateActivated(cal.getTime());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("dateActivated"));
		assertEquals("Order.error.dateActivatedInFuture", errors.getFieldError("dateActivated").getCode());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Order order = new Order();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);

		order.setOrderReasonNonCoded("orderReasonNonCoded");
		order.setAccessionNumber("accessionNumber");
		order.setCommentToFulfiller("commentToFulfiller");
		order.setVoidReason("voidReason");

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}

	@Test
	public void saveOrder_shouldNotSaveOrderIfInvalidOrderGroupEncounter() {
		executeDataSet(ORDER_SET);
		OrderGroup orderGroup = new OrderGroup();

		orderGroup.setEncounter(Context.getEncounterService().getEncounter(5));

		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000).withCareSetting(1)
		        .withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
		        .withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
		        .build();

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasErrors());
		// this group carries no patient at all, so BOTH group checks reject. Pinning each code keeps
		// the bare hasErrors() above from passing on an unrelated rejection.
		assertTrue(errors.hasFieldErrors("encounter"));
		assertEquals("Order.error.orderEncounterAndOrderGroupEncounterMismatch",
		    errors.getFieldError("encounter").getCode());
		assertTrue(errors.hasFieldErrors("patient"));
		assertEquals("Order.error.orderPatientAndOrderGroupPatientMismatch", errors.getFieldError("patient").getCode());
		assertEquals(2, errors.getErrorCount());
	}

	/**
	 * Isolates the group-encounter check: the group agrees with the order on the patient and differs
	 * only in the encounter, so the encounter mismatch is the sole rejection and cannot be satisfied by
	 * the group-patient check firing instead.
	 *
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfOrderEncounterDiffersFromOrderGroupEncounterOnAnOtherwiseValidOrder() {
		executeDataSet(ORDER_SET);
		Patient patient = Context.getPatientService().getPatient(7);
		Encounter orderEncounter = Context.getEncounterService().getEncounter(3);
		Encounter groupEncounter = Context.getEncounterService().getEncounter(5);
		assertNotEquals(orderEncounter, groupEncounter);

		OrderGroup orderGroup = new OrderGroup();
		orderGroup.setEncounter(groupEncounter);
		orderGroup.setPatient(patient);

		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000).withCareSetting(1)
		        .withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
		        .withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
		        .build();

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("encounter"));
		assertEquals("Order.error.orderEncounterAndOrderGroupEncounterMismatch",
		    errors.getFieldError("encounter").getCode());
		assertFalse(errors.hasFieldErrors("patient"));
		assertEquals(1, errors.getErrorCount());
	}

	/**
	 * @see OrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Order order = new Order();
		Encounter encounter = new Encounter();
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Patient patient = Context.getPatientService().getPatient(2);
		encounter.setPatient(patient);
		order.setPatient(patient);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setCareSetting(new CareSetting());
		order.setEncounter(encounter);
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);

		order.setOrderReasonNonCoded(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order.setAccessionNumber(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order.setCommentToFulfiller(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order.setVoidReason(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("accessionNumber"));
		assertTrue(errors.hasFieldErrors("orderReasonNonCoded"));
		assertTrue(errors.hasFieldErrors("commentToFulfiller"));
		assertTrue(errors.hasFieldErrors("voidReason"));
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("accessionNumber").getCode());
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("orderReasonNonCoded").getCode());
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("commentToFulfiller").getCode());
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("voidReason").getCode());
	}

	@Test
	public void saveOrder_shouldNotSaveOrderIfInvalidOrderGroupPatient() {
		executeDataSet(ORDER_SET);
		OrderGroup orderGroup = new OrderGroup();

		orderGroup.setEncounter(Context.getEncounterService().getEncounter(5));
		orderGroup.setPatient(Context.getPatientService().getPatient(2));

		Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000).withCareSetting(1)
		        .withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
		        .withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).withOrderGroup(orderGroup)
		        .build();

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("patient"));
		assertEquals("Order.error.orderPatientAndOrderGroupPatientMismatch", errors.getFieldError("patient").getCode());
	}

	/**
	 * Builds an order that satisfies every {@link OrderValidator} rule and deliberately leaves
	 * dateStopped, autoExpireDate and encounterDatetime unset, so a boundary test can vary exactly one
	 * date and attribute any rejection to that date alone.
	 *
	 * @param dateActivated the instant the boundary cases compare against
	 * @return a valid order awaiting a single date
	 */
	private Order newValidOrder(Date dateActivated) {
		Order order = new DrugOrder();
		Patient patient = Context.getPatientService().getPatient(2);
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setPatient(patient);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(new CareSetting());
		order.setUrgency(Order.Urgency.ROUTINE);
		order.setAction(Order.Action.NEW);
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setDateActivated(dateActivated);
		return order;
	}

	/**
	 * The rule is <code>dateActivated.after(dateStopped)</code>, so an identical instant is legal. Only
	 * an equality case distinguishes that from a &gt;= comparison.
	 *
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDateActivatedEqualsDateStopped() throws Exception {
		Date boundary = DateUtils.addDays(new Date(), -1);
		Order order = newValidOrder(boundary);
		OrderUtilTest.setDateStopped(order, boundary);
		assertEquals(order.getDateActivated(), order.getDateStopped());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * The rule is <code>dateActivated.after(autoExpireDate)</code>, so an identical instant is legal.
	 *
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDateActivatedEqualsAutoExpireDate() {
		Date boundary = DateUtils.addDays(new Date(), -1);
		Order order = newValidOrder(boundary);
		order.setAutoExpireDate(boundary);
		assertEquals(order.getDateActivated(), order.getAutoExpireDate());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * The rule is <code>encounterDatetime.after(dateActivated)</code>, so ordering at the exact moment
	 * of the encounter is legal.
	 *
	 * @see OrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDateActivatedEqualsEncounterDatetime() {
		Date boundary = DateUtils.addDays(new Date(), -1);
		Order order = newValidOrder(boundary);
		order.getEncounter().setEncounterDatetime(boundary);
		assertEquals(order.getDateActivated(), order.getEncounter().getEncounterDatetime());

		Errors errors = new BindException(order, "order");
		new OrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}
}
