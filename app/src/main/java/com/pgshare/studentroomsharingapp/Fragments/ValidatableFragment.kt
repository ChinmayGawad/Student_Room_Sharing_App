package com.pgshare.studentroomsharingapp.interfaces

interface ValidatableFragment {
    /**
     * Returns true if all inputs in the fragment are valid.
     * Also responsible for showing UI errors (like red text on TextInputs) if false.
     */
    fun isValid(): Boolean
}


