import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

object TdgVcsRoot : GitVcsRoot({
    name = "teamcity-Demo :: GitHub"
    url = "%repo.url%"
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
        -:refs/tags/*
    """.trimIndent()
    authMethod = password {
        userName = "x-access-token"
        password = "credentialsJSON:d3a28e96-4adf-436a-bbd6-493d3ba166b5"
    }
})
