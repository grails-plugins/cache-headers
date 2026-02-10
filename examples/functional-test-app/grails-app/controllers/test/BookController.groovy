package test

class BookController {

    static allowedMethods = [show: 'GET',]
    
    def show() {
        if ( !params.id) {
            render status: 422
            return
        }

        withCacheHeaders {
            assert request
            assert response
            assert params
            def book = Book.get(params.id)
            if ( !book ) {
                render 404
                return
            }
            etag {
                "${book.ident()}:${book.version}"
            }
            lastModified {
                book.lastUpdated
            }
            generate {
                render(view:"bookDisplay", model:[item:book])
            }
        }
    }
}