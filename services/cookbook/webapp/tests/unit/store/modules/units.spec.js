import {resource} from "@/store/modules/units";

describe("resource.url", () => {

    describe("with BASE_URL  /cookbook/", () => {
        beforeEach(() => {
            process.env.BASE_URL = "/cookbook/"
        })

        test("should start with base path", () => {
            expect(resource.url()).toEqual("/cookbook/units/groups")
        })
    })

    describe("with BASE_URL /cookbook/api/", () => {
        beforeEach(() => {
            process.env.BASE_URL = "/cookbook/api/"
        })

        test("should start with base path", () => {
            expect(resource.url()).toEqual("/cookbook/api/units/groups")
        })
    })
})