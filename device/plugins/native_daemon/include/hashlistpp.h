/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef HIPERF_HASHLIST_HPP
#define HIPERF_HASHLIST_HPP

#include "hashlist.h"

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
// implementation of template class LinkNode
template <typename Key, typename Val> LinkNode<Key, Val>::LinkNode(const Key& key) : key_ {key} {}

template <typename Key, typename Val>
LinkNode<Key, Val>::LinkNode(const Key& key, const Val& val) : key_ {key}, val_ {val}
{
}

template <typename Key, typename Val>
LinkNode<Key, Val>::LinkNode(const Key& key, Val&& val) : key_ {key}, val_ {std::move(val)}
{
}

template <typename Key, typename Val>
LinkNode<Key, Val>::LinkNode(const LinkNode& node) : link_ {node.link_}, key_  {node.key_}, val_ {node.val_}
{
}

template <typename Key, typename Val>
LinkNode<Key, Val>::LinkNode(LinkNode&& node)
    : link_ {std::move(node.link_)}, key_ {std::move(node.key_)}, val_ {std::move(node.val_)}
{
}

template <typename Key, typename Val> auto LinkNode<Key, Val>::operator=(const LinkNode& node) -> LinkNode<Key, Val>&
{
    link_ = node.link_;
    key_ = node.key_;
    val_ = node.val_;
}

template <typename Key, typename Val> auto LinkNode<Key, Val>::operator=(LinkNode&& node) -> LinkNode<Key, Val>&
{
    link_ = std::move(node.link_);
    key_ = std::move(node.key_);
    val_ = std::move(node.val_);
}

template <typename Key, typename Val> auto LinkNode<Key, Val>::GetLinkNode(Val* pval) -> LinkNode<Key, Val> *
{
    if (pval) {
        LinkNode<Key, Val>* pnode {nullptr};
        Val* offset = &pnode->val_;
        auto nodeAddr = reinterpret_cast<char*>(pval) - reinterpret_cast<char*>(offset);
        return reinterpret_cast<LinkNode<Key, Val>*>(nodeAddr);
    }
    return nullptr;
}

template <typename Key, typename Val> auto LinkNode<Key, Val>::GetLinkNode(Link* plink) -> LinkNode<Key, Val> *
{
    if (plink) {
        LinkNode<Key, Val>* pnode {nullptr};
        Link* offset = &pnode->link_;
        auto nodeAddr = reinterpret_cast<char*>(plink) - reinterpret_cast<char*>(offset);
        return reinterpret_cast<LinkNode<Key, Val>*>(nodeAddr);
    }
    return nullptr;
}
// end of LinkNode

// implementation of template class Iterator
template <typename Key, typename Val>
HashList<Key, Val>::Iterator::Iterator(LinkNode<Key, Val>* pnode, HashList* phashList)
    : pnode_ {pnode}, phashList_ {phashList}
{
    if (phashList_ == nullptr) {
        pnode_ = nullptr;
    }
}

template <typename Key, typename Val>
HashList<Key, Val>::Iterator::Iterator(const LinkNode<Key, Val>* pnode, const HashList* phashList)
    : pnode_ {const_cast<LinkNode<Key, Val>*>(pnode)}, phashList_ {const_cast<HashList*>(phashList)}
{
    if (phashList_ == nullptr) {
        pnode_ = nullptr;
    }
}

template <typename Key, typename Val>
HashList<Key, Val>::Iterator::Iterator(const Iterator& itr) : pnode_ {itr.pnode_}, phashList_ {itr.phashList_}
{
}

template <typename Key, typename Val>
HashList<Key, Val>::Iterator::Iterator(Iterator&& itr) : pnode_ {itr.pnode_}, phashList_ {itr.phashList_}
{
    itr.pnode_ = nullptr;
    itr.phashList_ = nullptr;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator=(const Iterator& itr) -> HashList<Key, Val>::Iterator&
{
    Iterator temp {itr};
    swap(temp);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator=(Iterator&& itr) -> HashList<Key, Val>::Iterator&
{
    Iterator temp {std::move(itr)};
    swap(temp);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator++() noexcept -> HashList<Key, Val>::Iterator&
{
    if (pnode_ == nullptr or phashList_ == nullptr) {
        phashList_ = nullptr;
        return *this;
    }
    Link* plink = pnode_->link_.next_;
    if (plink == &phashList_->tail_) {
        pnode_ = nullptr;
        return *this;
    }
    auto pnode = LinkNode<Key, Val>::GetLinkNode(plink);
    pnode_ = pnode;
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator++(int) noexcept -> HashList<Key, Val>::Iterator
{
    Iterator res {*this};
    if (pnode_ == nullptr or phashList_ == nullptr) {
        phashList_ = nullptr;
        return res;
    }
    Link* plink = pnode_->link_.next_;
    if (plink == &phashList_->tail_) {
        pnode_ = nullptr;
        return res;
    }
    auto pnode = LinkNode<Key, Val>::GetLinkNode(plink);
    pnode_ = pnode;
    return res;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator--() noexcept -> HashList<Key, Val>::Iterator&
{
    if (phashList_ == nullptr) {
        return *this;
    }
    Link* plink {nullptr};
    if (pnode_ == nullptr) {
        plink = phashList_->tail_.prev_;
    } else {
        plink = pnode_->link_.prev_;
    }
    if (plink == &phashList_->head_) {
        pnode_ = nullptr;
        phashList_ = nullptr;
        return *this;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::Iterator::operator--(int) noexcept -> HashList<Key, Val>::Iterator
{
    Iterator res {*this};
    if (phashList_ == nullptr) {
        return res;
    }
    Link* plink {nullptr};
    if (pnode_ == nullptr) {
        plink = phashList_->tail_.prev_;
    } else {
        plink = pnode_->link_.prev_;
    }
    if (plink == &phashList_->head_) {
        pnode_ = nullptr;
        phashList_ = nullptr;
        return res;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return res;
}

template <typename Key, typename Val>
bool HashList<Key, Val>::Iterator::operator<(const HashList<Key, Val>::Iterator& itr) const noexcept
{
    if (IsDangled() or itr.IsDangled()) {
        return false;
    }
    if (phashList_ != itr.phashList_) {
        return false;
    }
    Iterator tempItr {*this};
    if (tempItr == itr) {
        return false;
    }
    while (!tempItr.IsDangled()) {
        tempItr++;
        if (tempItr == itr) {
            return true;
        }
    }
    return false;
}

template <typename Key, typename Val>
bool HashList<Key, Val>::Iterator::operator==(const HashList<Key, Val>::Iterator& itr) const noexcept
{
    if (IsDangled() or itr.IsDangled()) {
        return false;
    }
    if (phashList_ != itr.phashList_) {
        return false;
    }
    return pnode_ == itr.pnode_;
}

template <typename Key, typename Val> Val& HashList<Key, Val>::Iterator::operator*()
{
    return pnode_->val_;
}

template <typename Key, typename Val> const Val& HashList<Key, Val>::Iterator::operator*() const
{
    return pnode_->val_;
}

template <typename Key, typename Val> Val* HashList<Key, Val>::Iterator::operator->()
{
    return &pnode_->val_;
}

template <typename Key, typename Val> const Val* HashList<Key, Val>::Iterator::operator->() const
{
    return &pnode_->val_;
}

template <typename Key, typename Val> void HashList<Key, Val>::Iterator::swap(HashList<Key, Val>::Iterator& other)
{
    using std::swap;
    swap(pnode_, other.pnode_);
    swap(phashList_, other.phashList_);
}
// end of Iterator

// Implementation of ReverseIterator
template <typename Key, typename Val>
HashList<Key, Val>::ReverseIterator::ReverseIterator(LinkNode<Key, Val>* pnode, HashList* phashList)
    : pnode_ {pnode}, phashList_ {phashList}
{
    if (phashList_ == nullptr) {
        pnode_ = nullptr;
    }
}

template <typename Key, typename Val>
HashList<Key, Val>::ReverseIterator::ReverseIterator(const LinkNode<Key, Val>* pnode, const HashList* phashList)
    : pnode_ {const_cast<LinkNode<Key, Val>*>(pnode)}, phashList_ {const_cast<HashList*>(phashList)}
{
    if (phashList_ == nullptr) {
        pnode_ = nullptr;
    }
}

template <typename Key, typename Val>
HashList<Key, Val>::ReverseIterator::ReverseIterator(const ReverseIterator& itr)
    : pnode_ {itr.pnode_}, phashList_ {itr.phashList_}
{
}

template <typename Key, typename Val>
HashList<Key, Val>::ReverseIterator::ReverseIterator(ReverseIterator&& itr)
    : pnode_ {itr.pnode_}, phashList_ {itr.phashList_}
{
    itr.pnode_ = nullptr;
    itr.phashList_ = nullptr;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator=(const ReverseIterator& itr) -> HashList<Key, Val>::ReverseIterator&
{
    ReverseIterator temp {itr};
    swap(temp);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator=(ReverseIterator&& itr) -> HashList<Key, Val>::ReverseIterator&
{
    ReverseIterator temp {std::move(itr)};
    swap(temp);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator++() noexcept -> HashList<Key, Val>::ReverseIterator&
{
    if (pnode_ == nullptr or phashList_ == nullptr) {
        phashList_ = nullptr;
        return *this;
    }
    Link* plink = &pnode_->link_;
    plink = plink->prev_;
    if (plink == &phashList_->head_) {
        pnode_ = nullptr;
        return *this;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator++(int) noexcept -> HashList<Key, Val>::ReverseIterator
{
    ReverseIterator res {*this};
    if (pnode_ == nullptr or phashList_ == nullptr) {
        phashList_ = nullptr;
        return res;
    }
    Link* plink = &pnode_->link_;
    plink = plink->prev_;
    if (plink == &phashList_->head_) {
        pnode_ = nullptr;
        return res;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return res;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator--() noexcept -> HashList<Key, Val>::ReverseIterator&
{
    if (phashList_ == nullptr) {
        return *this;
    }
    Link* plink {nullptr};
    if (pnode_ == nullptr) {
        plink = phashList_->head_.next_;
    } else {
        plink = pnode_->link_.next_;
    }
    if (plink == &phashList_->tail_) {
        pnode_ = nullptr;
        phashList_ = nullptr;
        return *this;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return *this;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::ReverseIterator::operator--(int) noexcept -> HashList<Key, Val>::ReverseIterator
{
    ReverseIterator res {*this};
    if (phashList_ == nullptr) {
        return res;
    }
    Link* plink {nullptr};
    if (pnode_ == nullptr) {
        plink = phashList_->head_.next_;
    } else {
        plink = pnode_->link_.next_;
    }
    if (plink == &phashList_->tail_) {
        pnode_ = nullptr;
        phashList_ = nullptr;
        return res;
    }
    pnode_ = LinkNode<Key, Val>::GetLinkNode(plink);
    return res;
}

template <typename Key, typename Val>
bool HashList<Key, Val>::ReverseIterator::operator<(const HashList<Key, Val>::ReverseIterator& itr) const noexcept
{
    if (IsDangled() or itr.IsDangled()) {
        return false;
    }
    if (phashList_ != itr.phashList_) {
        return false;
    }
    HashList<Key, Val>::ReverseIterator tempItr {*this};
    if (tempItr == itr) {
        return false;
    }
    while (!tempItr.IsDangled()) {
        tempItr++;
        if (tempItr == itr) {
            return true;
        }
    }
    return false;
}

template <typename Key, typename Val>
bool HashList<Key, Val>::ReverseIterator::operator==(const HashList<Key, Val>::ReverseIterator& itr) const noexcept
{
    if (IsDangled() or itr.IsDangled()) {
        return false;
    }
    if (phashList_ != itr.phashList_) {
        return false;
    }
    return pnode_ == itr.pnode_;
}

template <typename Key, typename Val> Val& HashList<Key, Val>::ReverseIterator::operator*()
{
    return pnode_->val_;
}

template <typename Key, typename Val> const Val& HashList<Key, Val>::ReverseIterator::operator*() const
{
    return pnode_->val_;
}

template <typename Key, typename Val> Val* HashList<Key, Val>::ReverseIterator::operator->()
{
    return &pnode_->val_;
}

template <typename Key, typename Val> const Val* HashList<Key, Val>::ReverseIterator::operator->() const
{
    return &pnode_->val_;
}

template <typename Key, typename Val>
void HashList<Key, Val>::ReverseIterator::swap(HashList<Key, Val>::ReverseIterator& other)
{
    using std::swap;
    swap(pnode_, other.pnode_);
    swap(phashList_, other.phashList_);
}
// end of ReverseIterator

// implementation of template class HashList
template <typename Key, typename Val> HashList<Key, Val>::HashList()
{
    head_.next_ = &tail_;
    tail_.prev_ = &head_;
}

template <typename Key, typename Val> HashList<Key, Val>::~HashList()
{
    while (head_.next_ != &tail_ and head_.next_ != nullptr) {
        LinkNode<Key, Val>* curLinkNode = LinkNode<Key, Val>::GetLinkNode(head_.next_);
        if (curLinkNode) {
            head_.next_ = curLinkNode->link_.next_;
            head_.next_->prev_ = &head_;
            delete curLinkNode;
            curLinkNode = nullptr;
        } else {
            break;
        }
    }
    head_.next_ = &tail_;
    tail_.prev_ = &head_;
    valueTab_.clear();
}

template <typename Key, typename Val> HashList<Key, Val>::HashList(const HashList& source)
{
    // in this implememtation, no feedback of copy failure,
    // user should check if copy succeeded
    Link* curLink = &head_;
    for (const auto& itr = source.begin(); itr < source.end(); ++itr) {
        LinkNode<Key, Val>* curNode = LinkNode<Key, Val>::GetLinkNode(*itr);
        if (curNode) {
            LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val> {*curNode};
            if (pnode) {
                curLink->next_ = pnode->link_;
                pnode->link_->prev_ = curLink;
                curLink = curLink->next_;
                valueTab_[pnode->key] = pnode;
                pnode = nullptr;
            } else {
                break;
            }
        } else {
            break;
        }
    }
    curLink->next_ = &tail_;
    tail_.prev_ = curLink;
}

template <typename Key, typename Val>
HashList<Key, Val>::HashList(HashList<Key, Val>&& source)
    : head_ {std::move(source.head_)}, tail_ {std::move(source.tail_)}, valueTab_ {std::move(source.valueTab_)}
{
    source = HashList();
}

template <typename Key, typename Val> auto HashList<Key, Val>::operator=(const HashList& source) -> HashList<Key, Val>&
{
    if (this == &source) {
        return *this;
    }
    Link* curLink = &head_;
    for (const auto& itr = source.begin(); itr < source.end(); ++itr) {
        LinkNode<Key, Val>* curNode = LinkNode<Key, Val>::GetLinkNode(*itr);
        if (curNode) {
            LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val> {*curNode};
            if (pnode) {
                curLink->next_ = pnode->link_;
                pnode->link_->prev_ = curLink;
                curLink = curLink->next_;
                valueTab_[pnode->key] = pnode;
                pnode = nullptr;
            } else {
                break;
            }
        } else {
            break;
        }
    }
    curLink->next_ = &tail_;
    tail_.prev_ = curLink;
    return *this;
}

template <typename Key, typename Val> auto HashList<Key, Val>::operator=(HashList&& source) -> HashList<Key, Val>&
{
    if (this == &source) {
        return *this;
    }
    head_ = std::move(source.head_);
    tail_ = std::move(source.tail_);
    valueTab_ = std::move(source.valueTab_);
    source = HashList();
    return *this;
}

template <typename Key, typename Val> auto HashList<Key, Val>::begin() -> HashList<Key, Val>::Iterator
{
    if (empty()) {
        return end();
    }
    return Iterator(LinkNode<Key, Val>::GetLinkNode(head_.next_), this);
}

template <typename Key, typename Val> auto HashList<Key, Val>::cbegin() const -> const HashList<Key, Val>::Iterator
{
    if (empty()) {
        return cend();
    }
    return Iterator(LinkNode<Key, Val>::GetLinkNode(head_.next_), this);
}

template <typename Key, typename Val> auto HashList<Key, Val>::end() -> HashList<Key, Val>::Iterator
{
    return Iterator(nullptr, this);
}

template <typename Key, typename Val> auto HashList<Key, Val>::cend() const -> const HashList<Key, Val>::Iterator
{
    return Iterator(nullptr, this);
}

template <typename Key, typename Val> auto HashList<Key, Val>::rbegin() -> HashList<Key, Val>::ReverseIterator
{
    if (empty()) {
        return rend();
    }
    return ReverseIterator(LinkNode<Key, Val>::GetLinkNode(tail_.prev_), this);
}

template <typename Key, typename Val>
auto HashList<Key, Val>::crbegin() const -> const HashList<Key, Val>::ReverseIterator
{
    if (empty()) {
        return crend();
    }
    return ReverseIterator(LinkNode<Key, Val>::GetLinkNode(tail_.prev_), this);
}

template <typename Key, typename Val> auto HashList<Key, Val>::rend() -> HashList<Key, Val>::ReverseIterator
{
    return ReverseIterator(nullptr, this);
}

template <typename Key, typename Val>
auto HashList<Key, Val>::crend() const -> const HashList<Key, Val>::ReverseIterator
{
    return ReverseIterator(nullptr, this);
}

template <typename Key, typename Val> Val& HashList<Key, Val>::front()
{
    if (empty()) {
        static Val temp {};
        return temp;
    }
    LinkNode<Key, Val>* pnode = LinkNode<Key, Val>::GetLinkNode(head_.next_);
    return pnode->val_;
}

template <typename Key, typename Val> const Val& HashList<Key, Val>::front() const
{
    return front();
}

template <typename Key, typename Val> Val& HashList<Key, Val>::back(bool prepend)
{
    if (empty()) {
        static Val temp {};
        return temp;
    }
    auto pnode = LinkNode<Key, Val>::GetLinkNode(tail_.prev_);
    if (prepend) {
        MoveToHead(pnode);
    }
    return pnode->val_;
}

template <typename Key, typename Val> Val& HashList<Key, Val>::operator[](const Key& key)
{
    LinkNode<Key, Val>* pnode {nullptr};
    if (valueTab_.find(key) == valueTab_.end()) {
        pnode = new (std::nothrow) LinkNode<Key, Val>(key);
    } else {
        pnode = valueTab_[key];
    }
    if (pnode) {
        MoveToHead(pnode);
    }
    return pnode->val_;
}

template <typename Key, typename Val> auto HashList<Key, Val>::find(const Key& key) -> HashList<Key, Val>::Iterator
{
    const auto& itr = valueTab_.find(key);
    if (itr == valueTab_.end()) {
        return end();
    }
    return Iterator(itr->second, this);
}

template <typename Key, typename Val> void HashList<Key, Val>::push_front(const Key& key, const Val& val)
{
    if (valueTab_.find(key) == valueTab_.end()) {
        LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val>(key, val);
        if (pnode) {
            MoveToHead(pnode);
            valueTab_[pnode->key_] = pnode;
        }
    } else {
        MoveToHead(valueTab_[key]);
        this->operator[](key) = val;
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::push_front(const Key& key, Val&& val)
{
    if (valueTab_.find(key) == valueTab_.end()) {
        LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val>(key, std::move(val));
        if (pnode) {
            MoveToHead(pnode);
            valueTab_[pnode->key_] = pnode;
        }
    } else {
        MoveToHead(valueTab_[key]);
        this->operator[](key) = val;
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::push_back(const Key& key, const Val& val)
{
    if (valueTab_.find(key) == valueTab_.end()) {
        LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val>(key, val);
        if (pnode) {
            MoveToTail(pnode);
            valueTab_[pnode->key_] = pnode;
        }
    } else {
        MoveToTail(valueTab_[key]);
        this->operator[](key) = val;
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::push_back(const Key& key, Val&& val)
{
    if (valueTab_.find(key) == valueTab_.end()) {
        LinkNode<Key, Val>* pnode = new (std::nothrow) LinkNode<Key, Val>(key, std::move(val));
        if (pnode) {
            MoveToTail(pnode);
            valueTab_[pnode->key_] = pnode;
        }
    } else {
        MoveToTail(valueTab_[key]);
        this->operator[](key) = val;
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::pop_front()
{
    if (empty()) {
        return;
    }
    LinkNode<Key, Val>* pnode = LinkNode<Key, Val>::GetLinkNode(head_.next_);
    valueTab_.erase(pnode->key_);
    EraseNode(head_.next_);
}

template <typename Key, typename Val> void HashList<Key, Val>::pop_back()
{
    if (empty()) {
        return;
    }
    LinkNode<Key, Val>* pnode = LinkNode<Key, Val>::GetLinkNode(tail_.prev_);
    valueTab_.erase(pnode->key_);
    EraseNode(tail_.prev_);
}

template <typename Key, typename Val>
auto HashList<Key, Val>::insert(const Iterator pos, const Key& key, const Val& val) -> HashList<Key, Val>::Iterator
{
    // assume pos is valid, otherwise the result is undefined
    LinkNode<Key, Val>* pnode {nullptr};
    if (valueTab_.find(key) == valueTab_.end()) {
        pnode = new (std::nothrow) LinkNode<Key, Val>(key, val);
        if (InsertNewNode(pos, pnode)) {
            valueTab_[key] = pnode;
            return Iterator(pnode, this);
        } else {
            return end();
        }
    }
    pnode = valueTab_[key];
    pnode->val_ = val;
    if (MoveNode(pos, pnode)) {
        return Iterator(pnode, this);
    }
    return end();
}

template <typename Key, typename Val>
auto HashList<Key, Val>::insert(const Iterator pos, const Key& key, Val&& val) -> HashList<Key, Val>::Iterator
{
    // assume pos is valid, otherwise the result is undefined
    LinkNode<Key, Val>* pnode {nullptr};
    if (valueTab_.find(key) == valueTab_.end()) {
        pnode = new (std::nothrow) LinkNode<Key, Val>(key, std::move(val));
        if (InsertNewNode(pos, pnode)) {
            valueTab_[key] = pnode;
            return Iterator(pnode, this);
        } else {
            return end();
        }
    }
    pnode = valueTab_[key];
    pnode->val_ = val;
    if (MoveNode(pos, pnode)) {
        return Iterator(pnode, this);
    }
    return end();
}

template <typename Key, typename Val> auto HashList<Key, Val>::erase(const Key& key) -> HashList<Key, Val>::Iterator
{
    if (valueTab_.find(key) == valueTab_.end()) {
        auto pos = end();
        ++pos;
        return pos;
    }
    LinkNode<Key, Val>* pnode = valueTab_[key];
    valueTab_.erase(key);
    Link* plink = pnode->link_.next_;
    Iterator tempItr {LinkNode<Key, Val>::GetLinkNode(plink), this};
    EraseNode(pnode);
    return tempItr;
}

template <typename Key, typename Val> auto HashList<Key, Val>::erase(const Iterator pos)
                                                                -> HashList<Key, Val>::Iterator
{
    // assume pos is valid, otherwise the result is undefined
    Iterator tempItr {pos};
    ++tempItr;
    LinkNode<Key, Val>* pnode = pos.GetNode();
    valueTab_.erase(pnode->key_);
    EraseNode(pnode);
    return tempItr;
}

template <typename Key, typename Val>
auto HashList<Key, Val>::erase(const Iterator first, const Iterator last) -> HashList<Key, Val>::Iterator
{
    // assume pos is valid, otherwise the result is undefined
    if (first <= last) {
        Iterator curPos {first};
        while (curPos < last) {
            curPos = erase(curPos);
        }
        return last;
    }
    auto pos = end();
    ++pos;
    return pos;
}

template <typename Key, typename Val>
bool HashList<Key, Val>::InsertNewNode(const Iterator& pos, LinkNode<Key, Val>*& pnode)
{
    if (pnode == nullptr) {
        return false;
    }
    using namespace std::rel_ops;
    if (begin() <= pos and pos <= end()) {
        Link* prevLink {nullptr};
        Link* nextLink {nullptr};
        Link* currLink {nullptr};
        if (pos == end()) {
            prevLink = tail_.prev_;
            nextLink = &tail_;
            currLink = &pnode->link_;
        } else {
            LinkNode<Key, Val>* pnextNode = pos.GetNode();
            nextLink = &pnextNode->link_;
            prevLink = pnextNode->link_.prev_;
            currLink = &pnode->link_;
        }
        prevLink->next_ = currLink;
        currLink->next_ = nextLink;
        nextLink->prev_ = currLink;
        currLink->prev_ = prevLink;
        return true;
    }
    delete pnode;
    return false;
}

template <typename Key, typename Val> bool HashList<Key, Val>::MoveNode(const Iterator& pos,
                                                                        LinkNode<Key, Val>*& pnode)
{
    if (pnode == nullptr) {
        return false;
    }
    Link* prevLink = pnode->link_.prev_;
    Link* nextLink = pnode->link_.next_;
    if (prevLink == nullptr or nextLink == nullptr) {
        return false;
    }
    prevLink->next_ = nextLink;
    nextLink->prev_ = prevLink;
    return InsertNewNode(pos, pnode);
}

template <typename Key, typename Val> void HashList<Key, Val>::MoveToHead(LinkNode<Key, Val>*& pnode)
{
    if (pnode) {
        if (pnode->link_.prev_ and pnode->link_.next_) {
            Link* prev = pnode->link_.prev_;
            Link* next = pnode->link_.next_;
            prev->next_ = next;
            next->prev_ = prev;
            pnode->link_.next_ = head_.next_;
            head_.next_->prev_ = &pnode->link_;
            head_.next_ = &pnode->link_;
            pnode->link_.prev_ = &head_;
        }
        if (!pnode->link_.prev_ and !pnode->link_.next_) {
            pnode->link_.next_ = head_.next_;
            head_.next_->prev_ = &pnode->link_;
            head_.next_ = &pnode->link_;
            pnode->link_.prev_ = &head_;
        }
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::MoveToTail(LinkNode<Key, Val>*& pnode)
{
    if (pnode) {
        if (pnode->link_.prev_ and pnode->link_.next_) {
            Link* prev = pnode->link_.prev_;
            Link* next = pnode->link_.next_;
            prev->next_ = next;
            next->prev_ = prev;
            pnode->link_.prev_ = tail_.prev_;
            tail_.prev_->next_ = &pnode->link_;
            pnode->link_.next_ = &tail_;
            tail_.prev_ = &pnode->link_;
        }
        if (!pnode->link_.prev_ and !pnode->link_.next_) {
            pnode->link_.prev_ = tail_.prev_;
            tail_.prev_->next_ = &pnode->link_;
            pnode->link_.next_ = &tail_;
            tail_.prev_ = &pnode->link_;
        }
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::EraseNode(LinkNode<Key, Val>*& pnode)
{
    if (pnode) {
        if (pnode->link_.prev_ and pnode->link_.next_) {
            Link* prev = pnode->link_.prev_;
            Link* next = pnode->link_.next_;
            prev->next_ = next;
            next->prev_ = prev;
        }
        delete pnode;
        pnode = nullptr;
    }
}

template <typename Key, typename Val> void HashList<Key, Val>::EraseNode(Link*& plink)
{
    LinkNode<Key, Val>* pnode = LinkNode<Key, Val>::GetLinkNode(plink);
    EraseNode(pnode);
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
#endif