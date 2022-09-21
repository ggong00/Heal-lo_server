import makeElements from "../module/create-elememt.js";

//DOM
const $reviewLists = document.querySelector('.review-lists');
const $orderBySelect = document.querySelector('.order-by-selector');

//페이지 첫 로딩 상태 값
let isFirst = true;

//운동시설 번호 전역변수
const fcno = document.querySelector('.facility').dataset.fcno;

// 페이지네이션 설정
const limitPage = 3;     //  페이지 최대 생성 수
const onePageNum = 10;    //  1페이지에 최대 목록 수
let currentPage = 1;      //  현재 페이지

//초기 페이지 세팅
reviewListRequest();
bookmarkCheck();

//작성버튼 클릭이벤트
document.querySelector('.review-write').addEventListener('click',e => location.href = `/reviews/${fcno}/add`)

//리뷰 정렬기준 선택 이벤트
$orderBySelect.addEventListener('change',() => reviewListRequest());

//즐겨찾기 클릭 이벤트
document.querySelector('.favorite-icon').addEventListener('click', (e) => {
    e.preventDefault();

    fetch(`/bookmarks/${fcno}`, {
        method:'PATCH',        //http method
        headers:{             //http header
            'Accept':'application/json'
        }
    })
        .then(response => response.json())
        .then(jsonData => {
            if (jsonData.header.code == '00') {
                e.target.style.color =
                    jsonData.data.status ? 'rgba(255, 155, 172, 0.462)' : `black`;
            } else if (jsonData.header.code == '03') {
                location.href = `/members/login?requestURI=${window.location.pathname}`;
            } else {
                throw new Error(jsonData.data);
            }
        })
        .catch(err => console.log(err));
});

/** 함수 **/

//운동시설 별점세팅
function getFacilityScore() {
    fetch(`/facilities/${fcno}/score`,{
        method : 'GET'
    })
        .then(response => response.json())
        .then(jsonData => {
            if(jsonData.header.code != '00') throw new Error(jsonData.data.message);
            //리뷰 평균 수정
            document.querySelector('.review-header__main .text-score')
                .textContent = jsonData.data.fcScore;
            document.querySelector('.review-header__main .inner-star')
                .style.width = jsonData.data.fcScore * 20 + '%'
        })
        .catch(error => console.log(error))
}

//리뷰리스트 기본 메세지
function createDefault() {
    return (
        `
        <div class="empty-message">
            <p>리뷰가 없습니다. 리뷰를 달아주세요!</p>
        </div>
        `
    )
}

//즐겨찾기 체크
function bookmarkCheck() {
    fetch(`/bookmarks`,{
        method : 'GET'
    })
        .then(response => response.json())
        .then(jsonData => {
            console.log(jsonData)
            if(jsonData.header.code != '00') throw new Error(jsonData.data.message);

            const found = jsonData.data.bookmarks.find(ele => ele.fcno == fcno);
            if(found != undefined) {
                document.querySelector('.favorite-icon').style.color = 'rgba(255, 155, 172, 0.462)';
            }

        })
        .catch(error => console.log(error))
}

//리뷰 검색
function reviewListRequest() {

    //검색 결과 수 조회
    fetch(`/reviews/${fcno}/total`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(jsonData => {
            currentPage = 1;
            //운동시설 별점 세팅
            getFacilityScore();

            //기존노드 삭제
            [...$reviewLists.children].forEach(ele => ele.remove());
            document.querySelector('.pagination-wrap')?.remove();

            //총 검색 결과 표시
            const $resultCount = document.querySelector('.review-cnt')
            $resultCount.textContent = jsonData.data.totalCount;

            //검색결과 0이면 기본 메세지
            if (jsonData.data.totalCount == 0) {
                $reviewLists.innerHTML = createDefault();
                return;
            }

            //페이지네이션 생성
            const totalPage = Math.ceil(jsonData.data.totalCount / onePageNum);
            const paginationWrap = createPagination(totalPage);
            $reviewLists.after(paginationWrap);

        })
        .catch(error => console.log(error));
}

//페이징 검색
function searchByPage(queryPram) {
    fetch(`/reviews/${fcno}/list` + queryPram,{
        method : 'GET'
    })
        .then(response => response.json())
        .then(jsonData => {

            // 이전 목록들 초기화
            [...$reviewLists.children].filter(ele => ele.classList.contains('review-card'))
                .forEach(ele => ele.remove());

            //새 목록 생성
            jsonData.data.reviews.forEach(ele => $reviewLists.appendChild(reviewListRender(ele)));

        })
        .catch(error => console.log(error))
}

//리뷰 삭제
function deleteReview(rvno) {
    fetch(`/reviews/${rvno}`, {
        method: `DELETE`
    })
        .then(response => response.json())
        .then(jsonData => {
            reviewListRequest()})
        .catch(error => console.log(error));
}

//리뷰 수정페이지
function toUpdatePage(rvno) {
    window.location = `/reviews/${rvno}/edit`
}

//리뷰 생성
function reviewListRender(data) {

    //리뷰 본문 미리보기 생성
    const previewContents = data.rvcontents.substr(0,200) + '...';
    const isMoreview = data.rvcontents.length > 200;

    const reviewCard =
    makeElements('div',{class : 'review-card', id : `${data.rvno}`},
        data.login ? makeElements('div',{class : 'review-btn-wrap'},
            makeElements('button',{class : 'review-update btn-review'},'수정'),
            makeElements('button',{class : 'review-delete btn-review'},'삭제')) : '',
        makeElements('div',{class : 'review-card__info'},
            makeElements('div',{class : 'rating-score review-card__star'},
                makeElements('div',{class : 'review-text-score'},`${data.rvscore}점`),
                makeElements('div',{class : 'outer-star'},'★★★★★',
                    makeElements('span',{class : 'inner-star'},'★★★★★'))),
            makeElements('span',{class : 'user-name'},data.memnickname),
            makeElements('span',{class : 'date'},data.rvcdate),
            makeElements('p',{class : 'preview-contents'},isMoreview ? previewContents : data.rvcontents,
            isMoreview ? makeElements('div',{class : 'btn-moreview'},'더보기') : ''),
            makeElements('div',{class : 'preview-wrap'})));

    reviewCard.querySelector('.inner-star').style.width = data.rvscore*20 + '%';

    //더보기 이벤트
    reviewCard.querySelector('.btn-moreview')?.addEventListener('click',(e) => {
        const contents = reviewCard.querySelector('.preview-contents');
        contents.classList.toggle('moreview-on');

        if(contents.classList.contains('moreview-on')) {
            contents.textContent = data.rvcontents;
        } else {
            contents.textContent = previewContents;
        }
    });

    //이미지 미리보기 생성
    data.imageFiles
        ?.forEach(ele => {
            const img =  document.createElement('img');
            img.setAttribute('src',`/images/${ele.code}/${ele.ufsname}`);
            img.setAttribute('data-bs-toggle','modal');
            img.setAttribute('data-bs-target','#modal');
            img.style.cursor = 'pointer';
            reviewCard.querySelector('.preview-wrap').appendChild(img);

            img.addEventListener('click',e => {
                document.getElementById('modal')
                    .querySelector('img').src = e.target.src;
            });
        });

    //리뷰 수정버튼 이벤트
    reviewCard.querySelector('.review-update')
        ?.addEventListener('click',() => toUpdatePage(data.rvno));

    //리뷰 삭제버튼 이벤트
    reviewCard.querySelector('.review-delete')
        ?.addEventListener('click', () => {
            if(!confirm("댓글을 삭제하시겠습니까?")) return;
            deleteReview(data.rvno)
        });


    return reviewCard;
}

//페이지 태그 생성
function createPagTag(text) {
    const page = document.createElement('li');
    page.setAttribute('class', 'page-item');
    const a = document.createElement('a');
    a.setAttribute('class', 'page-link shadow-none');
    a.textContent = text;
    page.appendChild(a);

    return {page : page, link : a};
}

//페이지네이션 생성
function createPagination(totalPage) {

    //이전 페이지네이션 삭제
    $reviewLists.querySelector('.pagination-wrap')?.remove();

    //페이지네이션 wrap생성
    const paginationWrap = document.createElement('li');
    const pagination = document.createElement('ul');
    paginationWrap.setAttribute('class', 'pagination-wrap');
    paginationWrap.setAttribute('aria-label', 'Page navigation example');
    pagination.setAttribute('class', 'pagination pagination-sm');
    paginationWrap.appendChild(pagination);

    //페이지 설정
    let pageLv = Math.ceil(currentPage/limitPage);
    let startIdx = currentPage;
    let lastIdx = currentPage + limitPage;

    //이전 페이지 생성
    if(pageLv != 1) {

        //페이지 태그 생성
        const {page,link} = createPagTag('이전');
        pagination.appendChild(page);

        //이전페이지 클릭 이벤트
        link.addEventListener('click', e => {
            currentPage = limitPage*pageLv - (limitPage*2-1);
            const paginationWrap = createPagination(totalPage);
            $reviewLists.after(paginationWrap);
        })
    }

    //페이지 생성
    for (startIdx; startIdx < lastIdx && startIdx <= totalPage; startIdx++) {

        //페이지 태그 생성
        const {page,link} = createPagTag(startIdx);
        pagination.appendChild(page);

        // 페이지 클릭 이벤트
        link.addEventListener('click', (e) => {
            e.preventDefault();

            //링크 설정
            e.target.textContent == 1 && isFirst ? location.href = '#' : location.href = '#review';

            //페이지 첫 로딩 상태 값
            isFirst = false;

            //현재 페이지 저장
            currentPage = parseInt(e.target.textContent);

            //페이징 검색
            const queryPram = `?pageNo=${currentPage}&numOfRow=${onePageNum}&orderBy=${$orderBySelect.value}`;
            searchByPage(queryPram);

            //클릭 표시
            [...paginationWrap.querySelectorAll('a')]
                .forEach(ele => ele.classList.remove('on'));
            e.target.classList.add('on');
        });

        //페이징 첫페이지 클릭
        link.textContent == currentPage && link.click();
    }

    //다음 페이지 생성
    if(pageLv != Math.ceil(totalPage/limitPage)) {

        //페이지 태그 생성
        const {page,link} = createPagTag('다음');
        pagination.appendChild(page);

        //다음페이지 클릭 이벤트
        link.addEventListener('click', e => {
            currentPage = limitPage*pageLv + 1;
            const paginationWrap = createPagination(totalPage);
            $reviewLists.after(paginationWrap);
        })
    }

    return paginationWrap;
}