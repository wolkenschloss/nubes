import {resource} from "@/store/modules/toc";

describe("resource", () => {
    beforeEach(() => {
        process.env.BASE_URL = "/cookbook/"
    })

    test.each([
        [{params: {from: 0}}, "/cookbook/recipe?from=0"],
        [{params: {to: 42}}, "/cookbook/recipe?to=42"],
        [{params: {from: 3, to: 7}}, "/cookbook/recipe?from=3&to=7"],
        [{}, "/cookbook/recipe"],
        [{params: {q: "qwe"}}, "/cookbook/recipe?q=qwe"]
    ])('url(%j)', (context, expected) => {
        expect(resource.url(context)).toBe(expected)
    })
})