declare module '*.properties' {
    /** A key/value object representing the content of a `.properties` file. */
    const properties: {
        /** The value of a `.properties` file key. */
        [key: string]: string
    }
    export { properties }
}
