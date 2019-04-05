package example.pages

import cz.kamenitxan.jakon.core.customPages.{AbstractStaticPage, StaticPage}

@StaticPage
class IndexPage extends AbstractStaticPage(templateName = "pages/index", url = "index") {

}
