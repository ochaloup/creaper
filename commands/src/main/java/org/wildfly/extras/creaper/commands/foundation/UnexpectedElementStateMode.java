package org.wildfly.extras.creaper.commands.foundation;

/**
 * <p>
 * Enumeration which could be used by commands to get specific
 * behavior when element is in state which is not expected.
 * <p>
 * Example: I want to add a datasource but some of that name already exists then<br>
 *  an exception can be thrown, the fact can be ignored, the existing one can be replaced
 */
public enum UnexpectedElementStateMode {
    /**
     * When element is in unexpected state
     *   it throws exception
     */
    EXCEPTION,
    
    /**
     * When element is in unexpected state
     *   it ignores, does nothing and returns back
     */
    IGNORE,
    
    /**
     * (meaningful for <i>Add</i> style commands)<br> 
     * When element is in unexpected state
     *   it replaces existing element
     */
    REPLACE
}
