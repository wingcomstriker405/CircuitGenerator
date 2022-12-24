package cg.common;

public class Assert
{
    public static void assertThat(boolean check, String message)
    {
        if (!check)
            throw new RuntimeException(message);
    }
}
