import {resource} from "@/store/modules/ingredients";

describe("resource", () => {
    beforeEach(() => {
        process.env.BASE_URL = "/cookbook/"
    })

    test.each([
        [{params: {from: 0}}, "/cookbook/ingredient?from=0"],
        [{params: {to: 42}}, "/cookbook/ingredient?to=42"],
        [{params: {from: 3, to: 7, q: "Zuc"}}, "/cookbook/ingredient?from=3&to=7&q=Zuc"],
        [{}, "/cookbook/ingredient"]
    ])('url(%j)', (context, expected) => {
        expect(resource.url(context)).toBe(expected)
    })
})