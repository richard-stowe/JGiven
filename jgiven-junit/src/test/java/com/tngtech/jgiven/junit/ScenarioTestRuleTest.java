package com.tngtech.jgiven.junit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import junitparams.internal.InvokeParameterisedMethod;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderFrameworkMethod;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.impl.NamedArgument;

@RunWith( DataProviderRunner.class )
public class ScenarioTestRuleTest {

    @DataProvider
    public static Object[][] methodTestData() throws Exception {
        return new Object[][] {
            // normal JUnit test
            { emptyStatement(), anyFrameworkMethod(), new Object(), new NamedArgument[0] },

            // junit-dataprovider test
            { emptyStatement(), dataProviderFrameworkMethod( twoParamsMethod(), "arg1", 2 ), new Object(),
                new NamedArgument[] { new NamedArgument( "s", "arg1" ), new NamedArgument( "i", 2 ) } },

            // junitparams test
            { junitParamsStatement( twoParamsMethod(), "arg1, 2" ), anyFrameworkMethod(), new Object(),
                new NamedArgument[] { new NamedArgument( "s", "arg1" ), new NamedArgument( "i", 2 ) } },

            // @Parameterized test
            { emptyStatement(), anyFrameworkMethod(), new ParameterizedTest( "test1", 4, false ),
                new NamedArgument[] { new NamedArgument( "s", "test1" ), new NamedArgument( "i", 4 ), new NamedArgument( "b", false ) } }, };
    }

    @Test
    @UseDataProvider( "methodTestData" )
    public void testParseMethodName( Statement statement, FrameworkMethod testMethod, Object target,
            NamedArgument[] expected ) {

        List<NamedArgument> result = ScenarioExecutionRule.getNamedArguments( statement, testMethod, target );
        assertThat( result ).containsExactly( expected );
    }

    // -- helper methods -----------------------------------------------------------------------------------------------

    private static Statement emptyStatement() {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {}
        };
    }

    private static FrameworkMethod anyFrameworkMethod() throws Exception {
        return new FrameworkMethod( twoParamsMethod() );
    }

    private static Object dataProviderFrameworkMethod( Method method, Object... args ) {
        return new DataProviderFrameworkMethod( method, 1, args );
    }

    private static Object junitParamsStatement( Method method, String args ) {
        return new InvokeParameterisedMethod( new FrameworkMethod( method ), ScenarioTestRuleTest.class, args, 1 );
    }

    private static Method twoParamsMethod() throws Exception {
        return getMethod( "testMethodWithTwoParams", String.class, int.class );
    }

    private static Method getMethod( String methodName, Class<?>... types ) throws Exception {
        return ScenarioTestRuleTest.class.getDeclaredMethod( methodName, types );
    }

    // -- mocks --------------------------------------------------------------------------------------------------------

    public void testMethodWithTwoParams( String s, int i ) {}

    @RunWith( Parameterized.class )
    public static class ParameterizedTest {
        private final static String S = "static";

        private final Object o = new Object();
        private final String s;
        private final double d = 5.0;
        private final int i;
        private final Boolean b;

        public ParameterizedTest( String s, int i, Boolean b ) {
            this.s = s;
            this.i = i;
            this.b = b;
        }
    }

}
