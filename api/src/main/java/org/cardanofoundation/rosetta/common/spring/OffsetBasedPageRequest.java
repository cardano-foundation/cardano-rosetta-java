package org.cardanofoundation.rosetta.common.spring;

import org.springframework.data.domain.Pageable;

public interface OffsetBasedPageRequest extends Pageable {

    /**
     * Returns the limit of items to be fetched.
     *
     * @return the limit
     */
    long getLimit();

}
