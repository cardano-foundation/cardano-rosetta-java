package org.cardanofoundation.rosetta.common.spring;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * Comprehensive test suite for {@link OffsetBasedPageRequest} class.
 * 
 * <p>This test class validates the custom implementation of Spring's {@code Pageable} interface
 * that provides offset-based pagination instead of Spring's default page-based pagination.
 * It handles the semantic difference between API offset/limit parameters ("skip N records, return M records") 
 * and Spring's page/size approach ("get page N with M records per page").</p>
 * 
 * <p>The test suite is organized into nested test classes covering:</p>
 * <ul>
 *   <li>Constructor validation and parameter handling</li>
 *   <li>Page number calculation logic</li>
 *   <li>Navigation methods (next, previous, first, specific page)</li>
 *   <li>State checking methods</li>
 *   <li>Object equality and hash code contracts</li>
 *   <li>String representation formatting</li>
 * </ul>
 * 
 * @see OffsetBasedPageRequest
 * @see org.springframework.data.domain.Pageable
 */
class OffsetBasedPageRequestTest {

    /**
     * Test cases for constructor validation and parameter handling.
     * 
     * <p>These tests ensure that the constructors properly validate input parameters,
     * handle edge cases, and initialize objects with correct default values.</p>
     */
    @Nested
    class ConstructorValidationTests {

        /**
         * SCENARIO: Create a basic OffsetBasedPageRequest with valid offset and limit.
         * 
         * <p>Tests the two-parameter constructor (offset, limit) to ensure it properly 
         * initializes the object with the provided values and sets a default unsorted Sort.</p>
         */
        @Test
        void shouldCreateValidOffsetBasedPageRequest() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            
            Assertions.assertThat(pageable.getOffset()).isEqualTo(10);
            Assertions.assertThat(pageable.getPageSize()).isEqualTo(5);
            Assertions.assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
        }

        /**
         * SCENARIO: Create an OffsetBasedPageRequest with custom sorting.
         * 
         * <p>Tests the three-parameter constructor (offset, limit, sort) to ensure it properly 
         * stores all provided values including the custom Sort criteria.</p>
         */
        @Test
        void shouldCreateValidOffsetBasedPageRequestWithSort() {
            Sort sort = Sort.by("name").descending();
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5, sort);
            
            Assertions.assertThat(pageable.getOffset()).isEqualTo(10);
            Assertions.assertThat(pageable.getPageSize()).isEqualTo(5);
            Assertions.assertThat(pageable.getSort()).isEqualTo(sort);
        }

        /**
         * SCENARIO: Attempt to create pagination with invalid negative offset.
         * 
         * <p>Tests that constructor validation rejects negative offset values and throws 
         * IllegalArgumentException with an appropriate error message.</p>
         */
        @Test
        void shouldThrowExceptionForNegativeOffset() {
            Assertions.assertThatThrownBy(() -> new OffsetBasedPageRequest(-1, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Offset must not be less than zero");
        }

        /**
         * SCENARIO: Attempt to create pagination with zero limit (no records per page).
         * 
         * <p>Tests that constructor validation rejects zero limit values since limit must be 
         * positive to represent a meaningful page size.</p>
         */
        @Test
        void shouldThrowExceptionForZeroLimit() {
            Assertions.assertThatThrownBy(() -> new OffsetBasedPageRequest(0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Limit must be greater than zero");
        }

        /**
         * SCENARIO: Attempt to create pagination with invalid negative limit.
         * 
         * <p>Tests that constructor validation rejects negative limit values and throws 
         * IllegalArgumentException since page size must be positive.</p>
         */
        @Test
        void shouldThrowExceptionForNegativeLimit() {
            Assertions.assertThatThrownBy(() -> new OffsetBasedPageRequest(0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Limit must be greater than zero");
        }

        /**
         * SCENARIO: Attempt to create pagination request with null sort parameter.
         * 
         * <p>Tests that constructor validation rejects null sort values and throws 
         * IllegalArgumentException with an appropriate error message.</p>
         */
        @Test
        void shouldHandleNullSort() {
            Assertions.assertThatThrownBy(() -> new OffsetBasedPageRequest(0, 5, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sort must not be null");
        }
    }

    /**
     * Test cases for page number calculation logic.
     * 
     * <p>These tests verify that the getPageNumber() method correctly converts 
     * offset-based pagination to page-based pagination using integer division.</p>
     */
    @Nested
    class PageNumberCalculationTests {

        /**
         * SCENARIO: Calculate which page number corresponds to offset 0 with limit 10.
         * 
         * <p>Tests that page number calculation returns 0 for the first page when 
         * offset is 0, regardless of the limit size.</p>
         */
        @Test
        void shouldCalculatePageNumberCorrectly_FirstPage() {
            // offset=0, limit=10 -> page 0
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(0, 10);
            Assertions.assertThat(pageable.getPageNumber()).isEqualTo(0);
        }

        /**
         * SCENARIO: Calculate page number for exactly the start of the second page.
         * 
         * <p>Tests that page number calculation returns 1 when offset equals the limit,
         * indicating the start of the second page.</p>
         */
        @Test
        void shouldCalculatePageNumberCorrectly_SecondPage() {
            // offset=10, limit=10 -> page 1
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 10);
            Assertions.assertThat(pageable.getPageNumber()).isEqualTo(1);
        }

        /**
         * SCENARIO: Calculate page number when offset falls within a page (not at page boundary).
         * 
         * <p>Tests that page number calculation uses integer division correctly when 
         * offset doesn't align exactly with page boundaries (offset=15, limit=10 → page 1).</p>
         */
        @Test
        void shouldCalculatePageNumberCorrectly_PartialPage() {
            // offset=15, limit=10 -> page 1 (15/10 = 1)
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(15, 10);
            Assertions.assertThat(pageable.getPageNumber()).isEqualTo(1);
        }

        /**
         * SCENARIO: Calculate page number for large offset values.
         * 
         * <p>Tests that page number calculation works correctly with larger numbers 
         * to ensure no integer overflow or calculation errors occur.</p>
         */
        @Test
        void shouldCalculatePageNumberCorrectly_LargeOffset() {
            // offset=100, limit=25 -> page 4
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(100, 25);
            Assertions.assertThat(pageable.getPageNumber()).isEqualTo(4);
        }
    }

    /**
     * Test cases for pagination navigation methods.
     * 
     * <p>These tests verify the navigation functionality including moving to next/previous pages,
     * jumping to first page, and navigating to specific page numbers.</p>
     */
    @Nested
    class NavigationTests {

        /**
         * SCENARIO: Generate pagination object for the next page of results.
         * 
         * <p>Tests that the next() method creates a new OffsetBasedPageRequest with 
         * offset increased by page size while preserving limit and sort settings.</p>
         */
        @Test
        void shouldCreateNextPageable() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            OffsetBasedPageRequest next = (OffsetBasedPageRequest) pageable.next();
            
            Assertions.assertThat(next.getOffset()).isEqualTo(15); // 10 + 5
            Assertions.assertThat(next.getPageSize()).isEqualTo(5);
            Assertions.assertThat(next.getSort()).isEqualTo(pageable.getSort());
        }

        // Note: previous() method not available in all Spring Data versions

        /**
         * SCENARIO: Generate pagination object for the first page from any current page.
         * 
         * <p>Tests that the first() method creates a new OffsetBasedPageRequest with 
         * offset=0 while preserving the original limit and sort settings.</p>
         */
        @Test
        void shouldCreateFirstPageable() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(50, 10);
            OffsetBasedPageRequest first = (OffsetBasedPageRequest) pageable.first();
            
            Assertions.assertThat(first.getOffset()).isEqualTo(0);
            Assertions.assertThat(first.getPageSize()).isEqualTo(10);
            Assertions.assertThat(first.getSort()).isEqualTo(pageable.getSort());
        }

        /**
         * SCENARIO: Jump directly to a specific page number (e.g., page 3).
         * 
         * <p>Tests that the withPage() method calculates the correct offset for a 
         * specified page number (page 3 with limit 10 → offset 30).</p>
         */
        @Test
        void shouldCreatePageableWithSpecificPageNumber() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(0, 10);
            OffsetBasedPageRequest page3 = (OffsetBasedPageRequest) pageable.withPage(3);
            
            Assertions.assertThat(page3.getOffset()).isEqualTo(30); // 3 * 10
            Assertions.assertThat(page3.getPageSize()).isEqualTo(10);
            Assertions.assertThat(page3.getSort()).isEqualTo(pageable.getSort());
        }

        /**
         * SCENARIO: Navigate to previous page when current page is not the first.
         * 
         * <p>Tests that previousOrFirst() reduces offset by page size when 
         * hasPrevious() returns true, effectively moving to the previous page.</p>
         */
        @Test
        void shouldCreatePreviousOrFirst_WhenHasPrevious() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            OffsetBasedPageRequest result = (OffsetBasedPageRequest) pageable.previousOrFirst();
            
            Assertions.assertThat(result.getOffset()).isEqualTo(5); // 10 - 5
            Assertions.assertThat(result.getPageSize()).isEqualTo(5);
        }

        /**
         * SCENARIO: Navigate to previous page when already on the first page.
         * 
         * <p>Tests that previousOrFirst() returns the first page (offset=0) when 
         * hasPrevious() returns false, preventing navigation beyond the first page.</p>
         */
        @Test
        void shouldCreatePreviousOrFirst_WhenNoPrevious() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(0, 5);
            OffsetBasedPageRequest result = (OffsetBasedPageRequest) pageable.previousOrFirst();
            
            Assertions.assertThat(result.getOffset()).isEqualTo(0); // same as first()
        }
    }

    /**
     * Test cases for pagination state checking methods.
     * 
     * <p>These tests verify the state query methods that determine whether 
     * navigation operations are available (e.g., hasPrevious()).</p>
     */
    @Nested
    class StateCheckTests {

        /**
         * SCENARIO: Check if there are previous pages when not on the first page.
         * 
         * <p>Tests that hasPrevious() returns true when offset > 0, indicating 
         * there are previous records that can be navigated to.</p>
         */
        @Test
        void shouldReturnTrueForHasPrevious_WhenOffsetGreaterThanZero() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            Assertions.assertThat(pageable.hasPrevious()).isTrue();
        }

        /**
         * SCENARIO: Check if there are previous pages when on the first page.
         * 
         * <p>Tests that hasPrevious() returns false when offset = 0, indicating 
         * we're on the first page with no previous records to navigate to.</p>
         */
        @Test
        void shouldReturnFalseForHasPrevious_WhenOffsetIsZero() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(0, 5);
            Assertions.assertThat(pageable.hasPrevious()).isFalse();
        }
    }

    /**
     * Test cases for object equality and hash code contracts.
     * 
     * <p>These tests ensure that the equals() and hashCode() methods follow 
     * the Java Object contract and properly compare OffsetBasedPageRequest instances.</p>
     */
    @Nested
    class EqualsAndHashCodeTests {

        /**
         * SCENARIO: Compare two pagination objects with identical parameters.
         * 
         * <p>Tests that equals() returns true and hashCode() returns the same value 
         * for objects with identical offset and limit values.</p>
         */
        @Test
        void shouldBeEqualWhenSameParameters() {
            OffsetBasedPageRequest pageable1 = new OffsetBasedPageRequest(10, 5);
            OffsetBasedPageRequest pageable2 = new OffsetBasedPageRequest(10, 5);
            
            Assertions.assertThat(pageable1).isEqualTo(pageable2);
            Assertions.assertThat(pageable1.hashCode()).isEqualTo(pageable2.hashCode());
        }

        /**
         * SCENARIO: Compare two pagination objects with identical parameters including sort.
         * 
         * <p>Tests that equals() and hashCode() properly consider the sort field 
         * in equality comparisons when all parameters match.</p>
         */
        @Test
        void shouldBeEqualWhenSameParametersWithSort() {
            Sort sort = Sort.by("name");
            OffsetBasedPageRequest pageable1 = new OffsetBasedPageRequest(10, 5, sort);
            OffsetBasedPageRequest pageable2 = new OffsetBasedPageRequest(10, 5, sort);
            
            Assertions.assertThat(pageable1).isEqualTo(pageable2);
            Assertions.assertThat(pageable1.hashCode()).isEqualTo(pageable2.hashCode());
        }

        /**
         * SCENARIO: Compare pagination objects differing only in offset.
         * 
         * <p>Tests that equals() returns false when objects have different 
         * offset values but identical limit and sort parameters.</p>
         */
        @Test
        void shouldNotBeEqualWhenDifferentOffset() {
            OffsetBasedPageRequest pageable1 = new OffsetBasedPageRequest(10, 5);
            OffsetBasedPageRequest pageable2 = new OffsetBasedPageRequest(15, 5);
            
            Assertions.assertThat(pageable1).isNotEqualTo(pageable2);
        }

        /**
         * SCENARIO: Compare pagination objects differing only in limit/page size.
         * 
         * <p>Tests that equals() returns false when objects have different 
         * limit values but identical offset and sort parameters.</p>
         */
        @Test
        void shouldNotBeEqualWhenDifferentLimit() {
            OffsetBasedPageRequest pageable1 = new OffsetBasedPageRequest(10, 5);
            OffsetBasedPageRequest pageable2 = new OffsetBasedPageRequest(10, 10);
            
            Assertions.assertThat(pageable1).isNotEqualTo(pageable2);
        }

        /**
         * SCENARIO: Compare pagination objects differing only in sort criteria.
         * 
         * <p>Tests that equals() returns false when objects have different 
         * sort fields but identical offset and limit parameters.</p>
         */
        @Test
        void shouldNotBeEqualWhenDifferentSort() {
            OffsetBasedPageRequest pageable1 = new OffsetBasedPageRequest(10, 5, Sort.by("name"));
            OffsetBasedPageRequest pageable2 = new OffsetBasedPageRequest(10, 5, Sort.by("date"));
            
            Assertions.assertThat(pageable1).isNotEqualTo(pageable2);
        }

        /**
         * SCENARIO: Test reflexive property of equals method.
         * 
         * <p>Tests that equals() returns true when comparing an object to itself, 
         * satisfying the reflexivity requirement of the equals contract.</p>
         */
        @Test
        void shouldBeEqualToItself() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            Assertions.assertThat(pageable).isEqualTo(pageable);
        }

        /**
         * SCENARIO: Compare pagination object with null reference.
         * 
         * <p>Tests that equals() returns false when comparing to null, 
         * ensuring null safety as required by the equals contract.</p>
         */
        @Test
        void shouldNotBeEqualToNull() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            Assertions.assertThat(pageable).isNotEqualTo(null);
        }

        /**
         * SCENARIO: Compare pagination object with object of different class.
         * 
         * <p>Tests that equals() returns false when comparing to an object 
         * of a different class type, ensuring type safety.</p>
         */
        @Test
        void shouldNotBeEqualToDifferentClass() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            Assertions.assertThat(pageable).isNotEqualTo("not a pageable");
        }
    }

    /**
     * Test cases for string representation formatting.
     * 
     * <p>These tests verify that the toString() method produces properly 
     * formatted, readable string representations of OffsetBasedPageRequest objects.</p>
     */
    @Nested
    class ToStringTests {

        /**
         * SCENARIO: Generate string representation of pagination object without custom sort.
         * 
         * <p>Tests that toString() includes offset, limit, and sort fields in a 
         * readable format when using the default unsorted Sort.</p>
         */
        @Test
        void shouldReturnCorrectStringRepresentation() {
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5);
            String result = pageable.toString();
            
            Assertions.assertThat(result).contains("offset=10");
            Assertions.assertThat(result).contains("limit=5");
            Assertions.assertThat(result).contains("sort=");
        }

        /**
         * SCENARIO: Generate string representation of pagination object with custom sort.
         * 
         * <p>Tests that toString() properly includes custom sort details 
         * (e.g., descending by name) in the formatted output string.</p>
         */
        @Test
        void shouldReturnCorrectStringRepresentationWithSort() {
            Sort sort = Sort.by("name").descending();
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5, sort);
            String result = pageable.toString();
            
            Assertions.assertThat(result).contains("offset=10");
            Assertions.assertThat(result).contains("limit=5");
            Assertions.assertThat(result).contains("sort=" + sort.toString());
        }
    }
}