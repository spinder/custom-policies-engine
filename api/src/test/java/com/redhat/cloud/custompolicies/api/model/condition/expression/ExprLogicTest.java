package com.redhat.cloud.custompolicies.api.model.condition.expression;

import org.hawkular.alerts.api.model.event.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprLogicTest {

    @Test
    public void testSingleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "facts.a = 'b' AND facts.b = 3";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "facts.a = 'c' AND facts.b = 3";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "facts.a = 'c' OR facts.b = 3";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "facts.a = 'b' OR facts.b = 3";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "facts.a = 'c' OR facts.b > 4";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    public void testSimpleBrackets() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "( facts.a = 'b' AND facts.b = 3 )";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND facts.b = 3)";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'c' AND facts.b = 3)";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'c')";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    public void testMultipleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "(facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND facts.b < 3)";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "((facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND facts.b < 3))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b > 2 OR (facts.c = '' AND facts.b < 3)))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b = 2 OR (facts.c = '' AND facts.b >= 3)))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 2)) OR (facts.c = '' AND facts.b >= 3))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 6)) OR (facts.c = '' AND facts.b < 3))";
        assertFalse(ExprParser.evaluate(event, expr));
    }

    @Test
    public void testNegativeMultipleOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b");
        factMap.put("b", 3);
        factMap.put("c", "");
        event.setFacts(factMap);

        String expr = "(facts.a = 'b' AND facts.b > 2) OR NOT (facts.c = '' AND facts.b < 3)";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "((facts.a = 'b' AND facts.b > 2) OR (facts.c = '' AND NOT facts.b > 3))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b > 2 OR (facts.c = '' AND facts.b < 3)))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(facts.a = 'b' AND (facts.b = 2 OR (facts.c = '' AND facts.b >= 3)))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND NOT (facts.b = 2)) OR (facts.c = '' AND facts.b >= 3))";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "(((facts.a = 'b') AND (facts.b = 6)) OR (facts.c = '' AND facts.b < 3))";
        assertFalse(ExprParser.evaluate(event, expr));

        expr = "(facts.c = '') AND NOT (facts.b <= 2)";
        assertTrue(ExprParser.evaluate(event, expr));

        expr = "!(facts.c = '' AND facts.a = 'b')";
        assertFalse(ExprParser.evaluate(event, expr));
    }
}
