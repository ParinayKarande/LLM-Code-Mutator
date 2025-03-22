package enums;

/**
 * List of available mutators.
 */
public enum Mutators {
    Conditionals_Boundary,
    Increments,
    Invert_Negatives,
    Math,
    Negate_Conditionals,
    Return_Values,
    Void_Method_Calls,
    Empty_Returns,
    False_Returns,
    True_Returns,
    Null_Returns,
    Primitive_Returns;


    public String getReadableName() {
        return this.name().replace("_", " ");
    }
}
