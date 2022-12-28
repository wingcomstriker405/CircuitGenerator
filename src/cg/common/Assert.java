package cg.common;

public class Assert
{
    private Assert() { }

    /**
     * Throws a {@link RuntimeException} if check is false.
     * @param check indicates if the assertion was successful
     * @param message the error message if check did not succeed
     */
    public static void assertThat(boolean check, String message)
    {
        if (!check)
            throw new RuntimeException(message);
    }
}
