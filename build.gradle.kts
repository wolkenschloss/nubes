buildscript {

}

plugins {
    idea
}

idea {
    module {
        excludeDirs.plusAssign(files(
            ".git-hooks",
            ".github",
            ".run",
            "gradle"))
    }
}