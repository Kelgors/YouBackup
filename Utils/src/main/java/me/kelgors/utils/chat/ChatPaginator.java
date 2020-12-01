package me.kelgors.utils.chat;

/**
 * Chat pagination helper
 * @author Kelgors
 *
 */
public class ChatPaginator {

    int mRowCount = 0;
    int mTotalPages = 0;

    String[] mPages = null;
    String mPageTitle = null;

    /**
     *
     * @param rowCount Number of lines per mPages
     */
    public ChatPaginator(int rowCount) {
        mRowCount = rowCount;
    }

    /**
     *
     * @param rowCount		Define the number of line per page (without title)
     * @param pages				The string array to paginate
     * @param titlepage 		Define a title for each page
     */
    public ChatPaginator(int rowCount, String[] pages, String titlepage) {
        mRowCount = rowCount;
        mPageTitle = titlepage;
        setArrayToPaginate(pages);
    }

    /**
     *
     * @param pages The string array to paginate
     */
    public void setArrayToPaginate(String[] pages) {
        mPages = pages;
        if (pages != null) {
            mTotalPages = pages.length / mRowCount;
            if (pages.length % mRowCount > 0.5f) {
                mTotalPages += 1;
            }
        } else {
            mTotalPages = -1;
        }
    }

    /**
     * Define a title for each page
     * @param pageTitle
     */
    public void setTitlePage(String pageTitle) {
        this.mPageTitle = pageTitle;
    }

    /**
     * Return an array of string to send to the player
     * @param page the number of the page you want
     * @return
     */
    public String[] getPage(int page) {
        String[] messages_page = null;
        if (mPages != null) {
            if (mTotalPages == -1) return null;
            if (page < 0) {
                return getPage(1);
            } else if (page > mTotalPages) {
                return getPage(mTotalPages);
            }
            page -= 1;
            boolean hasTitle = mPageTitle != null && mPageTitle.trim().length() > 0;
            int begin = page * mRowCount;
            int end = begin + mRowCount;
            // check page begin
            if (begin < 0) {
                begin = 0;
            } else if (begin > mPages.length) {
                begin = mTotalPages * mRowCount;
            }
            if (end < 0) {
                end = 0;
            } else if (end > mPages.length) {
                end = mPages.length;
            }


            messages_page = new String[(end - begin) + (hasTitle ? 1 : 0)];
            if (hasTitle) {
                messages_page[0] = mPageTitle + " (" + String.valueOf(page+1) + '/' + String.valueOf(mTotalPages) + ')';
            }
            System.arraycopy(mPages, begin, messages_page, 1, end - begin);
            return messages_page;
        }
        return null;
    }

}