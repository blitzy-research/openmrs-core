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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.CustomDosingInstructions;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Duration;
import org.openmrs.Encounter;
import org.openmrs.FreeTextDosingInstructions;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.TestOrder;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods on the {@link DrugOrderValidator} class.
 */
public class DrugOrderValidatorTest extends BaseContextSensitiveTest {

	@Autowired
	@Qualifier("adminService")
	AdministrationService adminService;

	/**
	 * A {@link DrugOrder} subclass, used to prove that {@link DrugOrderValidator#supports(Class)} also
	 * accepts subclasses of the class it declares support for.
	 */
	private class SomeDrugOrder extends DrugOrder {}

	/**
	 * Builds a drug order that validates with zero errors, so that a test can invalidate exactly one
	 * field and then assert the exact error code that field must produce. Keeping the rest of the order
	 * valid is what makes such an assertion meaningful: an unrelated error can no longer satisfy it.
	 *
	 * @return a fully populated drug order that passes validation
	 */
	private DrugOrder newValidDrugOrder() {
		DrugOrder order = new DrugOrder();
		Patient patient = Context.getPatientService().getPatient(2);
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setPatient(patient);
		order.setEncounter(encounter);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);
		return order;
	}

	/**
	 * @see DrugOrderValidator#supports(Class)
	 */
	@Test
	public void supports_shouldSupportDrugOrderAndItsSubclasses() {
		DrugOrderValidator validator = new DrugOrderValidator();

		assertTrue(validator.supports(DrugOrder.class));
		assertTrue(validator.supports(SomeDrugOrder.class));
	}

	/**
	 * @see DrugOrderValidator#supports(Class)
	 */
	@Test
	public void supports_shouldNotSupportClassesThatAreNotDrugOrders() {
		DrugOrderValidator validator = new DrugOrderValidator();

		assertFalse(validator.supports(Order.class));
		assertFalse(validator.supports(TestOrder.class));
		assertFalse(validator.supports(Object.class));
	}

	/**
	 * The validator delegates to {@link OrderValidator} before applying its own rules, so an order that
	 * is only invalid on a field owned by the superclass must still be rejected, with the superclass'
	 * own error code.
	 *
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldDelegateToTheOrderValidatorForInheritedFields() {
		DrugOrder order = newValidDrugOrder();
		// only OrderValidator rejects "encounter" for a patient mismatch; DrugOrderValidator never does
		order.getEncounter().setPatient(Context.getPatientService().getPatient(7));

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("encounter"));
		assertEquals("Order.error.encounterPatientMismatch", errors.getFieldError("encounter").getCode());
		assertEquals(1, errors.getErrorCount());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldApplyTheInheritedNotNullRulesOfTheOrderValidator() {
		DrugOrder order = newValidDrugOrder();
		order.setVoided(null);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("voided"));
		assertEquals("error.null", errors.getFieldError("voided").getCode());
		assertEquals(1, errors.getErrorCount());
	}

	/**
	 * A null order is rejected twice - once by {@link OrderValidator} through the delegating
	 * <code>super.validate</code> call and once by the validator's own guard - so the global error
	 * count also proves the delegation happens.
	 *
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDrugOrderIsNull() {
		Errors errors = new BindException(new DrugOrder(), "order");
		new DrugOrderValidator().validate(null, errors);

		assertTrue(errors.hasGlobalErrors());
		assertEquals(2, errors.getGlobalErrorCount());
		assertEquals("error.general", errors.getGlobalErrors().get(0).getCode());
		assertEquals("error.general", errors.getGlobalErrors().get(1).getCode());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRejectAnObjectThatIsNotADrugOrder() {
		DrugOrderValidator validator = new DrugOrderValidator();
		Order order = new Order();
		Errors errors = new BindException(order, "order");

		// supports(Class) is the guard that keeps a non drug order away from this validator
		assertFalse(validator.supports(Order.class));
		assertThrows(ClassCastException.class, () -> validator.validate(order, errors));
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfAsNeededIsNull() {
		DrugOrder order = new DrugOrder();
		order.setAsNeeded(null);
		order.setDrug(Context.getConceptService().getDrug(3));

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("asNeeded"));
		assertEquals("error.null", errors.getFieldError("asNeeded").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDosingTypeIsNull() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(null);
		order.setDrug(Context.getConceptService().getDrug(3));

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("dosingType"));
		assertEquals("error.null", errors.getFieldError("dosingType").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldNotFailValidationIfDrugIsNull() {
		DrugOrder order = new DrugOrder();
		order.setDrug(null);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertFalse(errors.hasFieldErrors("drug"));
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityIsNullForOutpatientCareSetting() {
		DrugOrder OutpatientOrder = new DrugOrder();
		OutpatientOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
		OutpatientOrder.setQuantity(null);
		Errors OutpatientOrderErrors = new BindException(OutpatientOrder, "order");
		new DrugOrderValidator().validate(OutpatientOrder, OutpatientOrderErrors);
		assertTrue(OutpatientOrderErrors.hasFieldErrors("quantity"));
		assertEquals("DrugOrder.error.quantityIsNullForOutPatient",
		    OutpatientOrderErrors.getFieldError("quantity").getCode());

		DrugOrder inPatientOrder = new DrugOrder();
		inPatientOrder.setCareSetting(Context.getOrderService().getCareSetting(2));
		inPatientOrder.setQuantity(null);
		Errors InpatientOrderErrors = new BindException(inPatientOrder, "order");
		new DrugOrderValidator().validate(inPatientOrder, InpatientOrderErrors);
		assertFalse(InpatientOrderErrors.hasFieldErrors("quantity"));
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNumberOfRefillsIsNullForOutpatientCareSetting() {
		DrugOrder OutpatientOrder = new DrugOrder();
		OutpatientOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
		OutpatientOrder.setNumRefills(null);
		Errors OutpatientOrderErrors = new BindException(OutpatientOrder, "order");
		new DrugOrderValidator().validate(OutpatientOrder, OutpatientOrderErrors);
		assertTrue(OutpatientOrderErrors.hasFieldErrors("numRefills"));
		assertEquals("DrugOrder.error.numRefillsIsNullForOutPatient",
		    OutpatientOrderErrors.getFieldError("numRefills").getCode());

		DrugOrder inPatientOrder = new DrugOrder();
		inPatientOrder.setCareSetting(Context.getOrderService().getCareSetting(2));
		inPatientOrder.setNumRefills(null);
		Errors InpatientOrderErrors = new BindException(inPatientOrder, "order");
		new DrugOrderValidator().validate(inPatientOrder, InpatientOrderErrors);
		assertFalse(InpatientOrderErrors.hasFieldErrors("numRefills"));
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotFailValidationIfQuantityAndNumRefillsAreNullIfConfiguredToNotRequire() {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_OUTPATIENT_QUANTITY, "false");

		DrugOrder OutpatientOrder = new DrugOrder();
		OutpatientOrder.setCareSetting(Context.getOrderService().getCareSetting(1));
		OutpatientOrder.setQuantity(null);
		OutpatientOrder.setQuantityUnits(null);
		OutpatientOrder.setNumRefills(null);
		Errors OutpatientOrderErrors = new BindException(OutpatientOrder, "order");
		new DrugOrderValidator().validate(OutpatientOrder, OutpatientOrderErrors);
		assertSame(CareSetting.CareSettingType.OUTPATIENT, OutpatientOrder.getCareSetting().getCareSettingType());
		assertFalse(OutpatientOrderErrors.hasFieldErrors("quantity"));
		assertFalse(OutpatientOrderErrors.hasFieldErrors("quantityUnits"));
		assertFalse(OutpatientOrderErrors.hasFieldErrors("numRefills"));
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseIsNullForSimpleDosingInstructionsDosingType() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("dose"));
		assertEquals("DrugOrder.error.doseIsNullForDosingTypeSimple", errors.getFieldError("dose").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNullForSimpleDosingInstructionsDosingType() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDoseUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("doseUnits"));
		assertEquals("DrugOrder.error.doseUnitsIsNullForDosingTypeSimple", errors.getFieldError("doseUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRouteIsNullForSimpleDosingInstructionsDosingType() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setRoute(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("route"));
		assertEquals("DrugOrder.error.routeIsNullForDosingTypeSimple", errors.getFieldError("route").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFrequencyIsNullForSimpleDosingInstructionsDosingType() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(SimpleDosingInstructions.class);
		order.setFrequency(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("frequency"));
		assertEquals("DrugOrder.error.frequencyIsNullForDosingTypeSimple", errors.getFieldError("frequency").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDosingInstructionsIsNullForFreeTextDosingInstructionsDosingType() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("dosingInstructions"));
		assertEquals("DrugOrder.error.dosingInstructionsIsNullForDosingTypeFreeText",
		    errors.getFieldError("dosingInstructions").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNullWhenDoseIsPresent() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDose(20.0);
		order.setDoseUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("doseUnits"));
		assertEquals("DrugOrder.error.doseUnitsRequiredWithDose", errors.getFieldError("doseUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseIsZero() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDose(0.0);
		order.setDoseUnits(Context.getConceptService().getConcept(51));
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("dose"));
		assertEquals("DrugOrder.error.doseZeroOrLess", errors.getFieldError("dose").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseIsNegative() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDose(-1.0);
		order.setDoseUnits(Context.getConceptService().getConcept(51));
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("dose"));
		assertEquals("DrugOrder.error.doseZeroOrLess", errors.getFieldError("dose").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDoseIsGreaterThanZero() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDose(1.0);
		order.setDoseUnits(Context.getConceptService().getConcept(51));
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("dose"));
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityUnitsIsNullWhenQuantityIsPresent() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setQuantity(20.0);
		order.setQuantityUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("quantityUnits"));
		assertEquals("DrugOrder.error.quantityUnitsRequiredWithQuantity", errors.getFieldError("quantityUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDurationUnitsIsNullWhenDurationIsPresent() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(20);
		order.setDurationUnits(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("durationUnits"));
		assertEquals("DrugOrder.error.durationUnitsRequiredWithDuration", errors.getFieldError("durationUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDrugConceptIsDifferentFromOrderConcept() {
		DrugOrder order = new DrugOrder();
		Drug drug = Context.getConceptService().getDrug(3);
		Concept concept = Context.getConceptService().getConcept(792);
		order.setDrug(drug);
		order.setConcept(concept); // the actual concept which matches with drug is "88"
		assertNotEquals(drug.getConcept(), concept);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("drug"));
		assertEquals("error.general", errors.getFieldError("drug").getCode());
		assertEquals("error.concept", errors.getFieldError("concept").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotRequireAllFieldsForADiscontinuationOrder() {
		DrugOrder orderToDiscontinue = (DrugOrder) Context.getOrderService().getOrder(111);
		assertTrue(OrderUtilTest.isActiveOrder(orderToDiscontinue, null));
		DrugOrder discontinuationOrder = new DrugOrder();
		discontinuationOrder.setDosingType(null);
		discontinuationOrder.setCareSetting(orderToDiscontinue.getCareSetting());
		discontinuationOrder.setConcept(orderToDiscontinue.getConcept());
		discontinuationOrder.setAction(Order.Action.DISCONTINUE);
		discontinuationOrder.setPreviousOrder(orderToDiscontinue);
		discontinuationOrder.setPatient(orderToDiscontinue.getPatient());
		discontinuationOrder.setDrug(orderToDiscontinue.getDrug());
		discontinuationOrder.setOrderType(orderToDiscontinue.getOrderType());
		discontinuationOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinuationOrder.setEncounter(Context.getEncounterService().getEncounter(3));

		Errors errors = new BindException(discontinuationOrder, "order");
		new DrugOrderValidator().validate(discontinuationOrder, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDoseUnitsIsNotADoseUnitConcept() {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugDosingUnits())));

		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("doseUnits"));
		assertEquals("DrugOrder.error.notAmongAllowedConcepts", errors.getFieldError("doseUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfQuantityUnitsItNotAQuantityUnitConcept() {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugDispensingUnits())));

		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("quantityUnits"));
		assertEquals("DrugOrder.error.notAmongAllowedConcepts", errors.getFieldError("quantityUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDurationUnitsIsNotADurationUnitConcept() {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDurationUnits())));

		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("durationUnits"));
		// a concept that is neither an allowed duration unit nor mapped to a SNOMED CT duration code is
		// rejected twice, and the order of those two rejections is part of the contract
		List<FieldError> durationUnitsErrors = errors.getFieldErrors("durationUnits");
		assertEquals(2, durationUnitsErrors.size());
		assertEquals("DrugOrder.error.notAmongAllowedConcepts", durationUnitsErrors.get(0).getCode());
		assertEquals("DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode", durationUnitsErrors.get(1).getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfRouteIsNotAValidConcept() {
		Concept concept = Context.getConceptService().getConcept(3);
		assertThat(concept, not(isIn(Context.getOrderService().getDrugRoutes())));

		DrugOrder order = new DrugOrder();
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDuration(5);
		order.setDurationUnits(concept);
		order.setDose(1.0);
		order.setDoseUnits(concept);
		order.setQuantity(30.0);
		order.setQuantityUnits(concept);
		order.setRoute(concept);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("route"));
		assertEquals("DrugOrder.error.routeNotAmongAllowedConcepts", errors.getFieldError("route").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsNullAndDrugIsNotSpecified() {
		DrugOrder order = new DrugOrder();

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("concept"));
		assertEquals("error.null", errors.getFieldError("concept").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsNullAndCannotInferItFromDrug() {
		DrugOrder order = new DrugOrder();
		Drug drug = Context.getConceptService().getDrug(3);
		drug.setConcept(null);
		order.setDrug(drug);

		Errors errors = new BindException(order, "order");
		adminService.validate(order, errors);
		assertTrue(errors.hasFieldErrors("concept"));
		assertEquals("error.null", errors.getFieldError("concept").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfConceptIsNullAndDrugIsSet() {
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setCareSetting(Context.getOrderService().getCareSetting(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		Drug drug = Context.getConceptService().getDrug(3);
		order.setDrug(drug);
		order.setConcept(null);
		FreeTextDosingInstructions di = new FreeTextDosingInstructions();
		di.setInstructions("testing");
		di.setDosingInstructions(order);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfConceptIsNullAndDrugNonCodedIsSet() {
		DrugOrder order = new DrugOrder();
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setCareSetting(Context.getOrderService().getCareSetting(2));
		order.setEncounter(Context.getEncounterService().getEncounter(3));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDrugNonCoded("non coded paracetamol");
		order.setConcept(null);
		FreeTextDosingInstructions di = new FreeTextDosingInstructions();
		di.setInstructions("testing");
		di.setDosingInstructions(order);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldApplyValidationForACustomDosingType() {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(CustomDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("brandName"));
		assertEquals("DrugOrder.error.brandNameIsNull", errors.getFieldError("brandName").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotValidateACustomDosingTypeAgainstAnyOtherDosingTypeValidation() {
		DrugOrder order = new DrugOrder();
		order.setDosingType(CustomDosingInstructions.class);
		order.setDosingInstructions(null);
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors("dosingInstructions"));
	}

	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldFailDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsTrue() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);

		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("drug"));
		assertEquals("DrugOrder.error.drugIsRequired", errors.getFieldError("drug").getCode());
	}

	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsFalse() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "false");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @throws SQLException
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Test
	public void saveOrder_shouldPassDrugOrderWithoutADrugWhenDrugOrderRequireDrugGBIsNotSet() throws SQLException {
		deleteAllData();
		baseSetupWithStandardDataAndAuthentication();
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		assertNull(Context.getAdministrationService()
		        .getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG));
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getConceptService().getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfDurationUnitsHasNoMappingToSNOMEDCTSource() {
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		ConceptService cs = Context.getConceptService();
		order.setConcept(cs.getConcept(5497));
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		order.setDuration(20);
		order.setDurationUnits(cs.getConcept(28));
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertEquals("DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode",
		    errors.getFieldError("durationUnits").getCode());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);

		order.setAsNeededCondition("asNeededCondition");
		order.setBrandName("brandName");

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see DrugOrderValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		DrugOrder order = new DrugOrder();
		Encounter encounter = new Encounter();
		Patient patient = Context.getPatientService().getPatient(2);
		order.setConcept(Context.getConceptService().getConcept(88));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("Instructions");
		order.setDosingInstructions("Test Instruction");
		order.setPatient(patient);
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());
		order.setAutoExpireDate(new Date());
		order.setOrderType(Context.getOrderService().getOrderTypeByName("Drug order"));
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setCareSetting(Context.getOrderService().getCareSetting(1));
		order.setQuantity(2.00);
		order.setQuantityUnits(Context.getConceptService().getConcept(51));
		order.setNumRefills(10);

		order.setAsNeededCondition(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		order.setBrandName(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("asNeededCondition"));
		assertTrue(errors.hasFieldErrors("brandName"));
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("asNeededCondition").getCode());
		assertEquals("error.exceededMaxLengthOfField", errors.getFieldError("brandName").getCode());
	}

	@Test
	public void saveOrder_shouldFailDrugOrderWithoutADrugNonCodedWhenDrugOrderIsNonCoded() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getOrderService().getNonCodedDrugConcept());
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors());
		assertEquals("DrugOrder.error.drugNonCodedIsRequired", errors.getFieldError("drugNonCoded").getCode());
	}

	@Test
	public void saveOrder_shouldPassDrugOrderWithADrugNonCodedWhenDrugOrderIsNonCoded() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setConcept(Context.getOrderService().getNonCodedDrugConcept());
		order.setDrugNonCoded("Non coded drug");
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	@Test
	public void saveOrder_shouldFailDrugOrderWithBothDrugNonCodedAndDrugAreSetForDrugOrder() {
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setDrug(Context.getConceptService().getDrug(3));
		order.setDrugNonCoded("paracetemol drug non coded");
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors());
		assertEquals("DrugOrder.error.onlyOneOfDrugOrNonCodedShouldBeSet", errors.getFieldError("concept").getCode());
	}

	@Test
	public void saveOrder_shouldFailDrugOrderWithNeitherDrugNonCodedNorDrugAreSetForDrugOrderWhenDrugRequiredSet() {
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true");
		Context.getAdministrationService().saveGlobalProperty(gp);
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors());
		assertEquals("DrugOrder.error.drugIsRequired", errors.getFieldError("drug").getCode());
	}

	@Test
	public void saveOrder_shouldPassDrugOrderWithNeitherDrugNonCodedNorDrugAreSetForDrugOrderWhenDrugRequiredISNotSet() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		CareSetting careSetting = Context.getOrderService().getCareSetting(2);
		OrderType orderType = Context.getOrderService().getOrderTypeByName("Drug order");

		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "false");
		Context.getAdministrationService().saveGlobalProperty(gp);
		//place drug order
		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(patient);
		order.setCareSetting(careSetting);
		order.setConcept(Context.getOrderService().getNonCodedDrugConcept());
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderType);
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setInstructions("None");
		order.setDosingInstructions("Test Instruction");
		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);
		assertFalse(errors.hasErrors());
		assertFalse(errors.hasFieldErrors());
	}

	/**
	 * Every other SimpleDosingInstructions test is negative, so on its own the suite would also be
	 * satisfied by a validator that rejected valid values. This freezes the complete positive path: an
	 * allowed dose unit, an allowed dispensing unit, an allowed route, a frequency and a duration unit
	 * that is both allowed and mapped to a SNOMED CT duration code.
	 *
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationForACompleteSimpleDosingInstructionsOrder() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-drugOrderAutoExpireDate.xml");
		ConceptService conceptService = Context.getConceptService();
		OrderService orderService = Context.getOrderService();
		Concept doseUnits = conceptService.getConcept(51);
		Concept route = conceptService.getConcept(22);
		Concept durationUnits = conceptService.getConcept(1001);
		OrderFrequency frequency = orderService.getOrderFrequency(1);
		// the positive path only proves anything if every unit really is an allowed concept
		assertTrue(orderService.getDrugDosingUnits().contains(doseUnits));
		assertTrue(orderService.getDrugDispensingUnits().contains(doseUnits));
		assertTrue(orderService.getDrugRoutes().contains(route));
		assertTrue(orderService.getDurationUnits().contains(durationUnits));
		assertNotNull(Duration.getCode(durationUnits));
		assertNotNull(frequency);

		DrugOrder order = new DrugOrder();
		Patient patient = Context.getPatientService().getPatient(2);
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setPatient(patient);
		order.setEncounter(encounter);
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setCareSetting(orderService.getCareSetting(1));
		order.setConcept(conceptService.getConcept(88));
		order.setDrug(conceptService.getDrug(3));
		order.setDosingType(SimpleDosingInstructions.class);
		order.setDose(1.0);
		order.setDoseUnits(doseUnits);
		order.setRoute(route);
		order.setFrequency(frequency);
		order.setDuration(5);
		order.setDurationUnits(durationUnits);
		order.setQuantity(2.00);
		order.setQuantityUnits(doseUnits);
		order.setNumRefills(10);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		order.setDateActivated(cal.getTime());

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * Isolates the branch that fires when the order concept IS the non coded drug concept: a coded drug
	 * may then not be set. Drug 100 is mapped to that very concept, so the unrelated drug/concept
	 * mismatch rejection cannot fire and mask the branch under test.
	 *
	 * @see DrugOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfACodedDrugIsSetForTheNonCodedDrugConcept() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-nonCodedDrugs.xml");
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, "true"));
		OrderService orderService = Context.getOrderService();
		Concept nonCodedDrugConcept = orderService.getNonCodedDrugConcept();
		Drug drug = Context.getConceptService().getDrug(100);
		assertEquals(nonCodedDrugConcept, drug.getConcept());

		DrugOrder order = new DrugOrder();
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		order.setEncounter(encounter);
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setCareSetting(orderService.getCareSetting(2));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setDateActivated(encounter.getEncounterDatetime());
		order.setOrderType(orderService.getOrderTypeByName("Drug order"));
		order.setDosingType(FreeTextDosingInstructions.class);
		order.setDosingInstructions("Test Instruction");
		// the non coded drug concept first, then a coded drug
		order.setConcept(nonCodedDrugConcept);
		order.setDrug(drug);

		Errors errors = new BindException(order, "order");
		new DrugOrderValidator().validate(order, errors);

		assertTrue(errors.hasFieldErrors("concept"));
		assertEquals("DrugOrder.error.onlyOneOfDrugOrNonCodedShouldBeSet", errors.getFieldError("concept").getCode());
		assertFalse(errors.hasFieldErrors("drugNonCoded"));
		assertEquals(1, errors.getErrorCount());
	}
}
