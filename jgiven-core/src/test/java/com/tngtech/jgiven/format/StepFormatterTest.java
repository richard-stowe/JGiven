package com.tngtech.jgiven.format;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.report.model.NamedArgument;
import com.tngtech.jgiven.report.model.StepFormatter;
import com.tngtech.jgiven.report.model.StepFormatter.Formatting;
import com.tngtech.jgiven.report.model.Word;

@RunWith( DataProviderRunner.class )
public class StepFormatterTest {

    @DataProvider
    public static Object[][] testCases() {
        return new Object[][] {
            { "a", asList(), "a" },
            { "a b", asList(), "a b" },
            { "", asList( "a" ), " a" },
            { "foo", asList( "a" ), "foo a" },
            { "", asList( "a", "b" ), " a b" },
            { "$", asList( "a" ), "a" },
            { "foo $", asList( "a" ), "foo a" },
            { "$ foo", asList( "a" ), "a foo" },
            { "foo $ foo", asList( "a" ), "foo a foo" },
            { "$ $", asList( "a", "b" ), "a b" },
            { "$d foo", asList( 5 ), "5 foo" },
            { "$1 foo $1", asList( "a" ), "a foo a" },
            { "$2 $1", asList( "a", "b" ), "b a" },
        };
    }

    @Test
    @UseDataProvider( "testCases" )
    public void formatter_should_handle_dollars_correctly( String source, List<Object> arguments, String expectedResult ) {
        testFormatter( source, arguments, null, null, expectedResult );
    }

    @DataProvider
    public static Object[][] formatterTestCases() {
        return new Object[][] {
            { "$", asList( true ), new NotFormatter(), "", "" },
            { "$", asList( false ), new NotFormatter(), "", "not" },
            { "$not$", asList( false ), new NotFormatter(), "", "not" },
            { "$or should not$", asList( false ), new NotFormatter(), "", "not" },
            { "$or not$", asList( false ), new NotFormatter(), "", "not" },
            { "$", asList( true ), null, "", "true" },
            { "$", asList( 5d ), new PrintfFormatter(), "%.2f", "5[.,]00" },
        };
    }

    @Test
    @UseDataProvider( "formatterTestCases" )
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public void testFormatter( String source, List<Object> arguments, ArgumentFormatter<?> formatter, String formatterArg,
            String expectedResult ) {
        List<Formatting<?>> asList = Lists.newArrayList();
        if( formatter != null ) {
            asList.add( new Formatting( formatter, formatterArg ) );
        } else {
            for( int i = 0; i < arguments.size(); i++ ) {
                asList.add( null );
            }
        }
        List<NamedArgument> namedArguments = Lists.newArrayList();
        for( Object o : arguments ) {
            namedArguments.add( new NamedArgument( "foo", o ) );
        }

        List<Word> formattedWords = new StepFormatter( source, namedArguments, asList )
            .buildFormattedWords();
        List<String> formattedValues = toFormattedValues( formattedWords );
        String actualResult = Joiner.on( ' ' ).join( formattedValues );
        assertThat( actualResult ).matches( expectedResult );
    }

    private List<String> toFormattedValues( List<Word> formattedWords ) {
        List<String> result = Lists.newArrayList();
        for( Word w : formattedWords ) {
            result.add( w.getFormattedValue() );
        }
        return result;
    }
}
