import {resource} from '@/store/modules/recipe'

describe("resource", () => {
    beforeEach(() => {
        process.env.BASE_URL = "/cookbook/"
    })

    test.each([
        [{id: 4711}, "/cookbook/recipe/4711"],
        [{id: 4712, servings: 4}, "/cookbook/recipe/4712?servings=4"],
        [{id: 4713, other: 42}, "/cookbook/recipe/4713"],
        [{}, "/cookbook/recipe/"]
    ])('url(%j)', (context, expected) => {
        expect(resource.url(context)).toBe(expected)
    })
})
