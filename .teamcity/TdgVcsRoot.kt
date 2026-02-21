import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

object TdgVcsRoot : GitVcsRoot({
    name = "tdg-devops-test :: GitHub"
    url = "%repo.url%"
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
        -:refs/tags/*
    """.trimIndent()
})
